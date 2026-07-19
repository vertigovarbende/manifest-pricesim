# Eşzamanlı Kripto Fiyat Simülatörü

## Proje Hakkında

Bu proje, bellek içinde çalışan bir Spring Boot concurrency simülatörüdür. Seed ile tekrarlanabilir fiyat güncelleme görevleri üretir; görevleri sınırlı bir blocking queue üzerinden sabit sayıda worker ile işler ve ortak sayaç ile coin state üzerinde oluşan race condition etkilerini gösterir. Aynı görev listesi unsafe ve safe implementasyonlarla çalıştırılarak sonuçlar, süre, throughput ve invariant raporları üzerinden karşılaştırılır.

BTC, ETH ve SOL için başlangıç fiyatları uygulama içinde tanımlıdır. Harici veritabanı kullanılmaz; son başarılı simülasyon ve safe coin snapshot'ları bellekte tutulduğu için uygulama yeniden başlatıldığında silinir.

## Kullanılan Teknolojiler

- Java 21
- Spring Boot 4.0.7
- Spring Web MVC
- Jakarta Bean Validation
- Maven Wrapper
- Git ve GitHub
- springdoc OpenAPI 3.0.3
- Lombok
- JUnit 5

## Uygulamayı Çalıştırma

1. Repoyu klonlayın:

   ```bash
   git clone https://github.com/vertigovarbende/manifest-pricesim.git
   cd manifest-pricesim
   ```

2. Java ve Maven'ın JDK 21 kullandığını doğrulayın:

   ```bash
   java -version
   ./mvnw -version
   ```

3. Testleri çalıştırın:

   ```bash
   ./mvnw clean test
   ```

4. Uygulamayı başlatın:

   ```bash
   ./mvnw spring-boot:run
   ```

   Alternatif olarak IntelliJ IDEA'da `pom.xml` dosyasını açıp `ConcurrencyApplication` sınıfını çalıştırabilirsiniz.

5. Uygulama varsayılan olarak `http://localhost:8080` adresinde açılır.

Windows'ta `./mvnw` yerine `mvnw.cmd` kullanılabilir.

## Swagger Adresi

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Endpoint'ler ve request modelleri Swagger UI üzerinden incelenip çalıştırılabilir.

## Endpoint'ler

| Endpoint | Ne yapar? |
|---|---|
| `POST /simulate?updates=10000&workers=4&seed=42&threadMode=PLATFORM` | Aynı görev listesini unsafe ve safe state/counter ile çalıştırır. `updates`: 1–100.000, `workers`: 1–16, `seed`: 1–42. `threadMode` opsiyoneldir ve `PLATFORM` veya `VIRTUAL` olabilir. Eşzamanlı ikinci simülasyon 409, geçersiz parametre 400 döner. |
| `GET /coins` | Son başarılı simülasyondaki safe coin snapshot'larını döndürür. Sonuç yoksa 404 döner. |
| `GET /stats` | Son simülasyonun beklenen, unsafe ve safe sonuçlarını; süre, throughput ve invariant raporlarını döndürür. Sonuç yoksa 404 döner. |
| `POST /benchmarks` | Worker sayıları için safe state/counter kullanarak platform ve virtual thread benchmark'ı çalıştırır; medyan süre ve throughput üretir. |
| `POST /deadlock/demo?mode=UNSAFE` | İki lock'ın ters sırada alınmasıyla deterministik deadlock senaryosu üretir ve JVM seviyesinde tespit eder. |
| `POST /deadlock/demo?mode=ORDERED` | Lock'ları global sırada alarak aynı transfer senaryosunu deadlock olmadan tamamlar. |

Örnek platform thread simülasyonu:

```bash
curl -X POST \
  'http://localhost:8080/simulate?updates=10000&workers=4&seed=42&threadMode=PLATFORM'
```

Örnek virtual thread simülasyonu:

```bash
curl -X POST \
  'http://localhost:8080/simulate?updates=10000&workers=4&seed=42&threadMode=VIRTUAL'
```

Örnek benchmark isteği:

```bash
curl -X POST 'http://localhost:8080/benchmarks' \
  -H 'Content-Type: application/json' \
  -d '{
    "updates": 50000,
    "workers": [1, 2, 4, 8],
    "seed": 42,
    "warmupRuns": 2,
    "measurementRuns": 5
  }'
```

Benchmark request kısıtları:

- `updates`: 1–100.000
- Her `workers` değeri: 1–8
- `warmupRuns`: 0–10
- `measurementRuns`: 1–20

Başarılı cevaplar `ConResponse<T>`, hatalar `ConErrorResponse` formatındadır. Hata mesajları `Accept-Language: tr` veya `Accept-Language: en` header'ına göre yerelleştirilir; varsayılan dil Türkçedir.

## Mimari Akış

```text
TaskGenerator
    |
    +--> Ortak görev listesi --> ExpectedResultCalculator
    |                               |
    |                               `--> Beklenen fiyat ve update count
    |
    +--> TaskProducer --> ArrayBlockingQueue --> N TaskConsumer
                                                |
                         +----------------------+----------------------+
                         |                                             |
                  UnsafeCoinState                              SafeCoinState
                  UnsafeTaskCounter                            SafeTaskCounter
                         |                                             |
                         +----------------------+----------------------+
                                                |
                                         InvariantChecker
                                                |
                                         SimulationResult
```

`SimulationController` HTTP sözleşmesini ve validasyonu yönetir. `SimulationService` görev üretimi, expected result hesabı, unsafe/safe run ve sonucun bellekte yayınlanmasını orkestre eder. `SimulationEngine`; executor, producer–consumer akışı, worker completion, timeout ve graceful shutdown sorumluluklarını taşır.

Paket sorumlulukları:

| Paket | Sorumluluk |
|---|---|
| `api` | Controller, request/response modelleri ve OpenAPI sözleşmesi |
| `service` | Simülasyon orkestrasyonu ve son sonuçların saklanması |
| `engine` | Queue, producer/consumer, executor, benchmark ve thread mode |
| `state` | Safe/unsafe coin state ve coin kataloğu |
| `counter` | Safe/unsafe işlenen görev sayacı |
| `metrics` | Expected result, invariant ve performans sonuçları |
| `deadlock` | Deadlock üretme, tespit ve ordered locking demosu |
| `exception` | Uygulama exception'ları ve merkezi HTTP hata yönetimi |

## Tasarım Kararları

| Karar noktası | Kararımız | Neden? (+alternatif karşılaştırması) |
|---|---|---|
| Görev kuyruğu | `ArrayBlockingQueue`, kapasite `min(requestedCapacity, 1000)` | Sabit kapasiteli, thread-safe ve FIFO bir queue sağlar. Queue dolunca producer'ı bloklayarak backpressure uygular ve sınırsız queue'ya göre bellek kullanımını öngörülebilir tutar. |
| Worker havuzu | Platform modunda `newFixedThreadPool(workers)` | Update başına thread açmak yerine sınırlı sayıda uzun ömürlü consumer kullanır. Thread/stack ve context switch maliyetini sınırlar. Virtual modda aynı consumer modeli `newThreadPerTaskExecutor` ile karşılaştırılır. |
| Güvenli sayaç | `AtomicLong` kullanan `SafeTaskCounter` | Tek alanlı increment işlemi için açık lock'tan daha sade ve yeterlidir. Unsafe karşılığı `value++` ile lost update problemini gösterir. |
| Coin kilidi | `ReentrantLock` | Coin'in fiyat, update count, son delta ve son güncelleyen thread alanlarını tek kritik bölümde tutarlı günceller. `synchronized` doğruluk sağlayabilirdi; `ReentrantLock` açık lock kapsamı ve genişletilebilir API sunar. |
| Lock kapsamı | Coin başına ayrı lock | Global lock'a göre daha yüksek paralellik sağlar; BTC güncellenirken ETH ve SOL bağımsız ilerleyebilir. Bedeli ayrı bir lock map'inin yönetilmesidir. |
| İşlerin tamamlanması | Worker başına poison pill + timeout'lu `Future.get` | Poison pill, `BlockingQueue.take()` üzerinde bekleyen her consumer'ın kontrollü çıkmasını sağlar. Future'lar worker hatalarını görünür kılar; tek global deadline toplam bekleme süresinin worker sayısıyla büyümesini önler. |
| Graceful shutdown | `shutdown` → `awaitTermination(1s)` → `shutdownNow` | Önce normal tamamlanmaya fırsat verir. Executor kapanmazsa thread ve kaynak sızıntısını önlemek için zorunlu kapatmaya geçer. |
| Sonucun paylaşılması | Immutable record/defensive copy + `AtomicReference` | Controller yalnızca tamamlanmış sonucu görür. Liste ve map kopyaları dışarıdan mutasyonu, atomic reference ise thread'ler arası görünürlük sorununu önler. |
| İkinci simülasyon isteği | `ReentrantLock.tryLock()` | İkinci `/simulate` isteğini bekletmeden 409 ile reddeder. Lock `finally` içinde bırakıldığı için hata durumunda kilitli kalmaz. `AtomicBoolean.compareAndSet` alternatif olabilirdi. |

Ek tasarım kararları:

- Safe ve unsafe run aynı seed ile bir kez üretilen aynı görev listesini kullanır; böylece girdi farkı ortadan kaldırılır.
- Expected result, state implementasyonlarından bağımsız olarak tek thread üzerinde hesaplanır; kontrol edilen kod kendi kendisini doğrulamaz.
- Benchmark'ta class loading/JIT etkisini azaltmak için sonuçlara katılmayan warm-up run'ları kullanılır.
- Ortalama yerine medyan süre raporlanarak tekil GC veya scheduler sıçramalarının etkisi azaltılır.
- Deadlock çözümünde bütün thread'ler lock'ları coin ID'sine göre aynı global sırada alır; circular wait koşulu ortadan kalkar.

10.000 görev için 10.000 platform thread açılmaz. Seçilen worker sayısı kadar consumer queue'dan görev alır:

```text
10.000 update + 4 worker = 4 uzun ömürlü consumer
```

Virtual thread modunda da deney koşullarını eşit tutmak için update başına değil worker sayısı kadar consumer oluşturulur.

## Race Condition Gözlemi

Projede iki bilinçli race noktası vardır:

1. `UnsafeTaskCounter` içindeki `value++`, tek ve atomik bir işlem değildir. Mevcut değeri okuma, bir artırma ve geri yazma adımlarından oluşur. İki thread aynı değeri okuyup aynı artırılmış değeri yazarsa artışlardan biri kaybolur.
2. `UnsafeCoinState`; `currentPrice`, `updateCount`, `lastDelta` ve `lastUpdatedBy` alanlarını lock olmadan değiştirir. Thread'ler aynı coin üzerinde iç içe geçtiğinde hem fiyat/update kaybı hem de birbirinden farklı task'lara ait alanların aynı snapshot'ta görünmesi mümkündür.

Teslim raporuna kaydedilmiş örnek gözlem:

```text
BTC    beklenen: 56.497 | güvenli: 56.497 ✓ | güvensiz: 56.331 ✗
Sayaç beklenen: 10.000 | güvenli: 10.000 ✓ | güvensiz: 9.784  ✗
```

Unsafe sonucun her çalışmada bozulması garanti değildir; race condition scheduler zamanlamasına bağlıdır. Worker ve görev sayısı arttıkça aynı state'e eşzamanlı erişim ihtimali yükselir.

## Güvenli Çözüm

Safe sayaç `AtomicLong.incrementAndGet()` kullanır. Coin state için tek tek atomik alanlar yeterli değildir; dört alanın birlikte aynı task'ı temsil etmesi gerekir. Bu nedenle `SafeCoinState`, her coin için ayrı bir `ReentrantLock` kullanır ve bütün state geçişini tek kritik bölümde gerçekleştirir:

```java
ReentrantLock lock = locks.get(task.coinId());
lock.lock();
try {
    MutableCoin coin = coins.get(task.coinId());
    coin.applyDelta(task.delta());
    return coin.snapshot();
} finally {
    lock.unlock();
}
```

Snapshot alma işlemi de aynı coin lock'ı altında yapılır. `try/finally`, update veya snapshot sırasında exception oluşsa bile lock'ın bırakılmasını garanti eder.

## Invariant ve Doğruluk Kanıtı

`ExpectedResultCalculator`, görev listesini concurrency olmadan sırayla işler ve her coin için matematiksel referans sonucu üretir:

```text
expectedPrice       = initialPrice + sum(coin'e ait bütün delta'lar)
expectedUpdateCount = coin'e ait görev sayısı
```

Her run sonunda `InvariantChecker` aşağıdaki koşulları kontrol eder:

```text
processedTaskCount == generatedTaskCount
actualUpdateCount  == expectedUpdateCount
actualPrice        == expectedPrice
```

Teslim raporundaki örnek çalışmada safe fiyat ve sayaç beklenen değerlerle eşleşmiş, unsafe BTC fiyatı ve görev sayacı sapmıştır. Response içindeki `InvariantReport`; genel `valid` değerine ek olarak `processedTaskCountMatches`, `coinUpdateCountsMatch`, `coinPricesMatch` ve ayrıntılı `violations` listesini döndürür.

Safe invariant'ın geçmesi beklenir. Unsafe invariant scheduler zamanlamasına göre geçebilir veya başarısız olabilir; unsafe sonucun mutlaka bozulması test assertion'ı yapılmamalıdır.

## Performans Sonuçları

Aşağıdaki değerler proje geliştirme sürecinde alınan performans ölçümleridir:

| Updates | Workers | Süre | Throughput (update/s) | Invariant |
|---:|---:|---:|---:|---|
| 50.000 | 1 | 114 ms | 435.233 | Başarılı |
| 50.000 | 4 | 28 ms | 1.762.563 | Başarılı |
| 50.000 | 8 | 81 ms | 611.987 | Başarılı |

Ölçümlerde 1 worker'dan 4 worker'a geçiş throughput'u artırmış, 8 worker ise 4 worker'dan daha yavaş kalmıştır. Bunun olası nedenleri coin başına lock contention, queue koordinasyonu, thread scheduling/context switch, JIT, GC ve ölçüm anındaki sistem yüküdür.

`POST /benchmarks`, her worker sayısını hem `PLATFORM` hem `VIRTUAL` modunda safe state/counter ile çalıştırır. Warm-up sonuçlarını dışarıda bırakır, ölçüm sürelerinin medyanını ve bu medyana göre throughput değerini raporlar.

## ReentrantLock ve synchronized Karşılaştırması

`synchronized`, safe coin state'in doğruluğunu sağlamak için yeterli olabilirdi ve lock bırakmayı JVM yönettiği için daha sade bir sözdizimine sahiptir. Bu projede `ReentrantLock`; lock sınırını açık göstermek ve `tryLock`, fairness ile kesintiye uğrayabilir `lockInterruptibly` gibi seçenekleri inceleyebilmek için tercih edilmiştir.

Ana simülasyonda `lock()` kullanılır; çünkü geçerli her coin güncellemesinin tamamlanması istenir. Aynı anda ikinci simülasyonu bekletmeden reddetmek için servis seviyesinde `tryLock()` kullanılır. Deadlock demosunda worker'ların timeout sonrasında durdurulabilmesi için `lockInterruptibly()` kullanılır.

Tek global coin lock yerine coin başına lock kullanılır. Global lock bütün coin güncellemelerini seri hâle getirirdi; coin başına lock ise BTC, ETH ve SOL güncellemelerinin birbirinden bağımsız ilerlemesine izin verir.

## Thread Dump İncelemesi

Thread dump, uygulama Java 21 ile çalışırken `PLATFORM` thread modunda; `100000` güncelleme ve `8` worker kullanan `/simulate` çağrısı devam ederken alınmıştır. Simülasyon kısa sürdüğü için aynı parametrelerle arka arkaya platform çağrıları çalıştırılmış ve JVM'nin anlık dump mekanizması kullanılmıştır.

Kullanılan komut:

```bash
kill -3 41863
```

Dump zamanı `2026-07-19 16:04:20` olarak kaydedilmiştir. İlgili gerçek çıktı kesiti şöyledir:

```text
"http-nio-8080-exec-7" ... waiting on condition
   java.lang.Thread.State: WAITING (parking)
        at java.util.concurrent.locks.ReentrantLock.lockInterruptibly(ReentrantLock.java:372)
        at java.util.concurrent.ArrayBlockingQueue.put(ArrayBlockingQueue.java:367)
        at com.manifest.concurrency.engine.TaskQueue.put(TaskQueue.java:26)
        at com.manifest.concurrency.engine.TaskProducer.produce(TaskProducer.java:22)
        at com.manifest.concurrency.engine.SimulationEngine.run(SimulationEngine.java:56)

"UNSAFE-worker-1" ... waiting on condition
   java.lang.Thread.State: WAITING (parking)
        at java.util.concurrent.ArrayBlockingQueue.take(ArrayBlockingQueue.java:420)
        at com.manifest.concurrency.engine.TaskQueue.take(TaskQueue.java:30)
        at com.manifest.concurrency.engine.TaskConsumer.consume(TaskConsumer.java:16)
        at com.manifest.concurrency.engine.SimulationEngine.lambda$run$0(SimulationEngine.java:54)

"UNSAFE-worker-4" ... waiting on condition
   java.lang.Thread.State: WAITING (parking)
        at java.util.concurrent.locks.ReentrantLock.lockInterruptibly(ReentrantLock.java:372)
        at java.util.concurrent.ArrayBlockingQueue.take(ArrayBlockingQueue.java:417)
        at com.manifest.concurrency.engine.TaskQueue.take(TaskQueue.java:30)
        at com.manifest.concurrency.engine.TaskConsumer.consume(TaskConsumer.java:16)
```

Çıktının analizi:

- Dump içinde `UNSAFE-worker-1` ile `UNSAFE-worker-8` arasındaki sekiz platform worker'ın tamamı görülmüştür. Bu sayı istekteki `workers=8` değeriyle aynıdır; 100000 update için update başına thread oluşturulmamıştır.
- HTTP request thread'inin `TaskProducer.produce()` üzerinden `ArrayBlockingQueue.put()` içinde beklemesi, bounded queue dolduğunda producer'a backpressure uygulandığını gösterir. Producer, consumer'lar kuyrukta yer açana kadar yeni task ekleyemez.
- `UNSAFE-worker-1`, `ArrayBlockingQueue.take()` içinde queue'nun `notEmpty` condition'ını beklemektedir. Örnekleme anında alabileceği bir task yoktur.
- `UNSAFE-worker-4`, aynı `take()` işlemi için `ArrayBlockingQueue` içindeki `ReentrantLock` kilidini edinmeyi beklemektedir. Bu, queue erişimindeki kısa süreli contention'ı gösterir; coin state'in unsafe veya safe olmasından bağımsız olarak queue kendi iç tutarlılığını kilitle korur.
- Dump içinde `Found one Java-level deadlock` bölümü bulunmamıştır; örnekleme anında JVM tarafından tespit edilen bir Java-level deadlock yoktur.

## Testler

Testleri çalıştırmak için:

```bash
./mvnw clean test
```

Mevcut otomatik testler:

- `ConcurrencyApplicationTests`: Spring context'in başarıyla açıldığını doğrular.
- `DeadlockServiceTest.unsafeModeShouldTriggerDeadlockScenario`: Unsafe senaryonun deadlock olarak tespit edildiğini doğrular.
- `DeadlockServiceTest.orderedModeShouldCompleteWithoutDeadlock`: Ordered locking senaryosunun tamamlandığını doğrular.

Toplam üç otomatik test bulunmaktadır ve testlerin tamamı başarıyla geçmektedir.

## Grup Üyeleri ve Katkıları

Aşağıdaki tablo uygulama geliştirme katkılarını ve proje kapsamında yürütülen araştırma görevlerini göstermektedir.

| Üye | Sorumluluk                                                                                     | Branch                                                                                                                   | Pull Request                                                                                                                                                                                                                                                                               | Review               |
|---|------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------|
| Batuhan Pınar | Coin state, expected result, API, Swagger ve locale                                            | `feature/TASK-3/add-safe-and-unsafe-states`, `feature/TASK-3/add-expected-calculator`, `feature/TASK-5/add-controller`   | [PR #4](https://github.com/vertigovarbende/manifest-pricesim/pull/4), [PR #5](https://github.com/vertigovarbende/manifest-pricesim/pull/5), [PR #9](https://github.com/vertigovarbende/manifest-pricesim/pull/9)                                                                           | Erdem Yusuf Kaya     |
| Erdem Yusuf Kaya | Counter, task generation, producer/consumer, simulation engine, benchmark ve graceful shutdown | `feature/TASK-4/add-producer-and-consumer`, `feature/TASK-4/throughput-calculation`, `refactor/TASK-4/graceful-shutdown` | [PR #3](https://github.com/vertigovarbende/manifest-pricesim/pull/3), [PR #6](https://github.com/vertigovarbende/manifest-pricesim/pull/6), [PR #12](https://github.com/vertigovarbende/manifest-pricesim/pull/12), [PR #13](https://github.com/vertigovarbende/manifest-pricesim/pull/13) | Batuhan Pınar        |
| Münevver Verim | Virtual thread desteği ve deadlock/ordered locking bonusu                                      | `feature/TASK-7/add-virtual-threads`, `feature/BONUS-C/add-deadlock-demo`                                                | [PR #10](https://github.com/vertigovarbende/manifest-pricesim/pull/10), [PR #14](https://github.com/vertigovarbende/manifest-pricesim/pull/14)                                                                                                                                             | Erdem Yusuf Kaya     |
| Esma Nilay Ülker |  |  |  |  |
| Mehmet Poyraz |  |  |  |  |
| Beytullah Kamacı |  |  |  |  |

Repo: https://github.com/vertigovarbende/manifest-pricesim

## Bonus Çalışmalar

### Java 21 Virtual Threads

`ThreadMode` ile `/simulate` çağrısında `PLATFORM` veya `VIRTUAL` seçilebilir. Platform modunda fixed thread pool, virtual modda isimlendirilmiş virtual thread factory ile `newThreadPerTaskExecutor` kullanılır. Her iki modda da worker sayısı kadar consumer çalıştırıldığı için aynı iş dağıtım modeli altında thread türlerinin maliyeti karşılaştırılır.

`POST /benchmarks` endpoint'i her worker sayısını iki thread modunda da safe state/counter ile çalıştırır. Warm-up run'larını ölçüm dışında tutar ve measurement sürelerinin medyanını raporlar.

### Deadlock ve Lock Ordering

`POST /deadlock/demo?mode=UNSAFE`, iki thread'in BTC ve ETH lock'larını ters sırada almasını `CountDownLatch` ile senkronize ederek circular wait oluşturur. İki saniyelik timeout sonrasında JVM `ThreadMXBean` ile deadlock tespit edilir ve interruptible lock bekleyişleri durdurulur.

`POST /deadlock/demo?mode=ORDERED`, bütün transferlerde lock'ları coin ID'sine göre aynı sırada alır. Böylece deadlock'un dört koşulundan circular wait ortadan kaldırılır ve iki transfer de tamamlanır.
