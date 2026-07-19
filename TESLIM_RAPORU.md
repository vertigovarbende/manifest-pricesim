# Teslim Raporu — Eşzamanlı Kripto Fiyat Simülatörü

## 1. Grup Bilgileri

| Alan | Bilgi                                                                                         |
|---|-----------------------------------------------------------------------------------------------|
| Grup adı | Manifest                                                                                      |
| Grup üyeleri (5) | Batuhan Pınar,Erdem Yusuf Kaya,Münevver Verim,Esma Nilay Ülker,Mehmet Poyraz,Beytullah Kamacı |
| GitHub repo linki | <https://github.com/vertigovarbende/manifest-pricesim.git>                                    |
| Pull Request linkleri | <https://github.com/vertigovarbende/manifest-pricesim/pulls>                                  |
| Conflict çözülen dosya | <...>                                                                                         |
| Conflict çözüm commit / PR | <...>                                                                                         |
| Yapılan bonus (varsa) | virtual threads/deadlock                                                                      |

## 2. Kısa Açıklama

Proje, eşzamanlı kripto fiyat güncellemelerinde oluşabilecek race condition problemlerini gösteren bir Spring Boot uygulamasıdır.

Aynı fiyat güncelleme görevlerini çoklu thread kullanarak iki farklı şekilde çalıştırır:

- Unsafe çalışma: Ortak coin verileri kilit kullanılmadan güncellenir.
- Safe çalışma: Güncellemeler ReentrantLock ve thread-safe sayaçlarla korunur.

Uygulama iki çalışmanın sonuçlarını, işlem sürelerini ve doğruluk durumlarını karşılaştırır. Aynı seed kullanılarak aynı görevlerin üretilmesi sayesinde sonuçlar tekrarlanabilir ve güvenli çalışmanın
beklenen değerleri sağlayıp sağlamadığı kontrol edilebilir.

REST API üzerinden simülasyon başlatılabilir, son coin durumları ve simülasyon istatistikleri görüntülenebilir. Swagger/OpenAPI desteğiyle endpoint’ler belgelenmiştir. 

## 3. Çalıştırma (özet)

```
git clone <https://github.com/vertigovarbende/manifest-pricesim.git>
cd <manifest-pricesim>
mvn spring-boot:run
# Swagger: http://localhost:8080/swagger-ui/index.html
# Deneyin: POST /simulate?updates=10000&workers=4&seed=42  ->  GET /stats -> GET /coins
```

## 4. Tasarım Kararları (özet)

| Konu | Karar ve gerekçe |
|---|---|
| Kuyruk | Sabit kapasiteli `ArrayBlockingQueue` kullanıldı. FIFO işleme, kontrollü bellek kullanımı ve producer üzerinde backpressure sağlandı. |
| Worker havuzu | Platform modunda `newFixedThreadPool(workers)` kullanıldı. Görev başına thread açmak yerine sınırlı sayıda worker görevleri kuyruktan tüketir. |
| Coin kilidi | Birden fazla coin alanını aynı kritik bölümde tutarlı güncellemek için `ReentrantLock` tercih edildi. |
| Lock kapsamı | Global lock yerine coin başına lock kullanılarak farklı coinlerin paralel güncellenmesine izin verildi. |
| Tamamlanma ve shutdown | Her worker için poison pill gönderildi; `Future.get` ve timeout ile tamamlanma izlendi. Executor önce normal, gerekirse zorunlu olarak kapatıldı. |

## 5. Race Condition Kanıtı

```
"invariant": {
        "valid": false,
        "processedTaskCountMatches": false,
        "coinUpdateCountsMatch": false,
        "coinPricesMatch": false,
        "violations": [
          "Processed task counter expected=50000, actual=49893",
          "BTC update count expected=16499, actual=16488",
          "BTC price expected=56497, actual=56331",
          "ETH update count expected=16840, actual=16833",
          "ETH price expected=5286, actual=5136",
          "SOL update count expected=16661, actual=16649",
          "SOL price expected=-6618, actual=-6262"
        ]
      }
```

## 6. Metrik Özeti

Aşağıdaki değerler proje geliştirme sürecinde alınan performans ölçümleridir:

| Updates | Workers | Süre | Throughput (update/s) | Invariant |
|---:|---:|---:|---:|---|
| 50.000 | 1 | 114 ms | 435.233 | Başarılı |
| 50.000 | 4 | 28 ms | 1.762.563 | Başarılı |
| 50.000 | 8 | 81 ms | 611.987 | Başarılı |

## 7. Thread Dump Özeti

Java 21 ve `PLATFORM` modunda, 100.000 güncelleme ile 8 worker çalışırken thread dump alındı. Dump içinde `UNSAFE-worker-1` ile `UNSAFE-worker-8` arasındaki sekiz worker görüldü. Worker'lar `ArrayBlockingQueue.take()` üzerinde görev veya queue kilidini beklerken `WAITING (parking)` durumundaydı. Producer'ın `ArrayBlockingQueue.put()` içinde beklemesi bounded queue backpressure davranışını gösterdi. JVM seviyesinde deadlock tespit edilmedi; ayrıntılı stack kesiti ve analiz README'de yer almaktadır.

## 8. Zorunlu Özellikler — Öz Değerlendirme

- [x] /simulate, /coins, /stats çalışıyor
- [x] Geçersiz parametre → HTTP 400, ikinci eşzamanlı istek → HTTP 409
- [x] Aynı görev listesi (immutable, tek üretim) safe ve unsafe'de kullanılıyor
- [x] BlockingQueue + sabit thread pool (her görev için yeni thread yok)
- [x] Güvensiz sürüm hatayı gösteriyor; güvenli sürüm invariant'ı sağlıyor
- [x] En az bir yerde ReentrantLock kullanıldı
- [x] Graceful shutdown; işlerin bitmesi bekleniyor
- [x] Seed ile tekrarlanabilir görev üretimi
- [x] throughput/süre + 1/2/4/8 worker tablosu
- [ ] Thread dump alındı ve README'de yorumlandı
- [x] Swagger çalışıyor, adres README'de
- [ ] Unit + en az 1 integration test
- [x] En az 3 branch, 2 PR, 2 review, 1 çözülmüş conflict

## 9. Bireysel Katkı Tablosu

| Üye | Rol / Ne yaptı? | Branch | PR | Review |
|---|---|---|---|---|
| Batuhan, Yusuf | Coin & State | `feature/TASK-3/add-safe-and-unsafe-states` | [PR #4](https://github.com/vertigovarbende/manifest-pricesim/pull/4) | Yusuf |
| Yusuf, Batuhan | Worker Pool | `feature/TASK-4/add-producer-and-consumer` | [PR #6](https://github.com/vertigovarbende/manifest-pricesim/pull/6) | Batuhan |
| Münevver, Yusuf | Simulation Engine & Invariant | `feature/TASK-4/add-simulation-engine-class` | [PR #8](https://github.com/vertigovarbende/manifest-pricesim/pull/8) | Batuhan |
| Batuhan, Münevver | API & Swagger | `feature/TASK-5/add-controller` | [PR #9](https://github.com/vertigovarbende/manifest-pricesim/pull/9) | Yusuf |
| Yusuf, Münevver | Metrik & Benchmark | `feature/TASK-4/throughput-calculation` | [PR #12](https://github.com/vertigovarbende/manifest-pricesim/pull/12) | Batuhan |

## 10. Notlar

Takım çalışması kısmında zorlandık, takım içi eşzamanlı çalışma ve zaman yönetimi konusunda zorlandık.
Worker sınıfını Runnable olarak değiştirmek isterdik ve daha fazla refactor yapılması gerektiğini düşünüyoruz.
