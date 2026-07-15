# TASKS.md

# Concurrency Project Roadmap

## Proje Bilgileri

| Alan | Değer |
|------|-------|
| Grup İsmi | Manifest |
| Proje İsmi | Concurrency |
| Java Versiyonu | Java 21 |

---

# Yapılacaklar

## 0. Refactoring

### Amaç

Projedeki mevcut kod yapısını daha okunabilir, sürdürülebilir ve genişletilebilir hale getirmek.

### Yapılacaklar

- [ ] Genel kod refactoring
- [ ] Kod tekrarlarının azaltılması
- [ ] SOLID prensiplerinin gözden geçirilmesi
- [ ] Package bağımlılıklarının sadeleştirilmesi
- [ ] Naming standartlarının düzenlenmesi
- [ ] Gereksiz kodların temizlenmesi

# 1. GitHub

## Pull Request Template

- [ ] PULL_REQUEST_TEMPLATE.md hazırlanacak

İçerik örneği

- Yapılan değişiklikler
- Neden yapıldı
- Test edildi mi?
- Ekran görüntüsü (varsa)
- Checklist

---
# 2. Package Standardı

Proje package yapısı standart hale getirilecek.

Package yapı:

```text
com.manifest.concurrency
├── api
│   ├── controller
│   ├── dto
│   │   ├── response
│   └── docs
├── common
│   ├── config
│   ├── exception
├── model/    
├── engine/   
├── state/    
├── counter/  
└── metrics/  
│
└── test
```

---

# 3. 'state' Package Tasarımı

State package'i yeniden tasarlanacak.

## Yapılacaklar

### Coin Modeli

- [ ] Coin modeli oluşturulması
- [ ] Coin üzerindeki işlemlerin belirlenmesi
- [ ] Ortak domain modelinin oluşturulması

### Unsafe State

- [ ] Thread-safe olmayan state implementasyonu
- [ ] Race condition örnekleri

### Safe State

- [ ] Thread-safe state implementasyonu
- [ ] synchronized / Lock yapılarının değerlendirilmesi

### Coin Başına Lock

- [ ] Coin bazlı lock mekanizması
- [ ] Global lock kullanımından kaçınılması
- [ ] ConcurrentHashMap + Lock yaklaşımı

---

# 4. Producer - Consumer Tasarımı

Producer / Consumer mimarisi yeniden düzenlenecek.

## Yapılacaklar

### Görev Üretimi

- [ ] Task modeli oluşturulması
- [ ] Producer yapısının düzenlenmesi

### BlockingQueue

- [ ] BlockingQueue kullanımı
- [ ] Queue yönetimi

### Worker Yapısı

- [ ] Worker Thread tasarımı
- [ ] Worker sayısının yönetilmesi

### Tamamlanma Mekanizması

- [ ] Task tamamlanma kontrolü
- [ ] Graceful shutdown
- [ ] Bekleyen görevlerin tamamlanması

---

# 5. REST API & Swagger

## REST API

- [ ] Controller katmanı
- [ ] Request DTO'ları
- [ ] Response DTO'ları
- [ ] Bean Validation

## Exception Handling

- [ ] Global Exception Handler
- [ ] Validation Exception
- [ ] Business Exception
- [ ] Standard Error Response

## Swagger

- [ ] OpenAPI yapılandırması
- [ ] Endpoint açıklamaları
- [ ] Request/Response örnekleri

## Logging

- [ ] ConResponse içerisindeki code alanının loglanması
- [ ] ConErrorResponse içerisindeki code alanının log.error ile yazdırılması

---

# 6. Testler

## Unit Test

- [ ] Unsafe state testleri
- [ ] Safe state testleri
- [ ] Coin bazlı lock testleri
- [ ] Race condition testleri
- [ ] Service testleri
- [ ] State testleri
- [ ] Producer testleri
- [ ] Consumer testleri
- [ ] Worker testleri

## Integration Test

- [ ] API testleri
- [ ] Producer-Consumer entegrasyonu
- [ ] Queue testleri

## Stress Test

- [ ] Çok sayıda thread ile test
- [ ] Race condition analizi
- [ ] Performans ölçümü

## Thread Dump

- [ ] Thread dump alınması
- [ ] Analiz dokümantasyonu

---

# Bonus Görevler

## Virtual Threads (Java 21)

- [ ] Virtual Thread kullanımı
- [ ] Platform Thread karşılaştırması
- [ ] Benchmark

---

## Deadlock

- [ ] Deadlock senaryosu oluşturulması
- [ ] Deadlock analizi
- [ ] Çözüm yöntemlerinin gösterilmesi

---

## CompletableFuture

- [ ] Async işlemler
- [ ] thenApply
- [ ] thenCompose
- [ ] allOf
- [ ] anyOf
- [ ] Exception handling

---

# Ekstra

## Basit Arayüz

- [ ] Basit web arayüzü
- [ ] Görev üretme
- [ ] Görev durumlarını görüntüleme
- [ ] Coin durumlarını görüntüleme
- [ ] Worker durumlarını görüntüleme

---

# Genel Checklist

- [ ] Refactoring
- [ ] State package
- [ ] Coin modeli
- [ ] Safe state
- [ ] Unsafe state
- [ ] Coin lock yapısı
- [ ] Producer
- [ ] Consumer
- [ ] BlockingQueue
- [ ] Worker yapısı
- [ ] Completion mekanizması
- [ ] REST API
- [ ] DTO
- [ ] Validation
- [ ] Exception Handling
- [ ] Swagger
- [ ] Logging
- [ ] Package standardı
- [ ] PR Template
- [ ] Unit Test
- [ ] Integration Test
- [ ] Stress Test
- [ ] Thread Dump
- [ ] Virtual Threads
- [ ] Deadlock
- [ ] CompletableFuture
- [ ] Basit UI