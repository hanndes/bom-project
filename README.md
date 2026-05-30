# 🏭 BOM Project

**Bill of Materials (BOM) otomasyon ve yönetim** REST API'si. Spring Boot 3 ile geliştirilmiş olup üretim veya ürün geliştirme süreçlerinde ürün yapılarını, bileşen hiyerarşilerini ve malzeme gereksinimlerini yönetmek için tasarlanmıştır.

## 🚀 Teknoloji Yığını

| Katman | Teknoloji |
|---|---|
| Dil | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Güvenlik | Spring Security + JWT (jjwt 0.11.5) |
| Veritabanı | PostgreSQL |
| Önbellek | Redis |
| ORM | Spring Data JPA |
| API Dokümantasyonu | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |
| Container | Docker + Docker Compose |

## 📋 Özellikler

- JWT tabanlı kimlik doğrulama ve yetkilendirme
- BOM katman yönetimi (oluşturma, güncelleme, silme)
- Güvenli tekrar denemeler için idempotency desteği
- Performans için Redis önbellekleme
- API keşfi için Swagger UI
- Kolay dağıtım için Dockerize edilmiş yapı

## ⚙️ Gereksinimler

- Java 21+
- Docker & Docker Compose
- Maven (veya `./mvnw` kullanın)

## 🛠️ Başlarken

### 1. Repoyu klonlayın

```bash
git clone https://github.com/hanndes/bom-project.git
cd bom-project
```

### 2. `.env` dosyası oluşturun

Proje kökünde `.env` dosyası oluşturun:

```env
# JWT
JWT_SECRET=buraya_secret_yazin
JWT_ACCESS_TOKEN_TTL=3600

# Veritabanı
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=sifreniz

# Redis
REDIS_HOST=localhost
REDIS_PORT=6380
```

> Güvenli JWT secret üretmek için: `openssl rand -base64 64`

### 3. Docker Compose ile çalıştırın

```bash
docker-compose up --build
```

### 4. Yerel ortamda çalıştırın (Docker olmadan)

PostgreSQL ve Redis çalışıyor olmalı, ardından:

```bash
./mvnw spring-boot:run
```

## 📖 API Dokümantasyonu

Uygulama çalıştıktan sonra şu adrese gidin:

```
http://localhost:8080/swagger-ui/index.html
```

## 🗂️ Proje Yapısı

```
src/
└── main/
    └── java/com/handederelii/bomproject/
        ├── controller/
        ├── service/
        ├── repository/
        ├── model/
        ├── dto/
        └── security/
```

## 🔐 Ortam Değişkenleri

| Değişken | Açıklama |
|---|---|
| `JWT_SECRET` | JWT imzalama için gizli anahtar |
| `JWT_ACCESS_TOKEN_TTL` | Token geçerlilik süresi (saniye) |
| `DB_URL` | PostgreSQL JDBC URL'i |
| `DB_USERNAME` | Veritabanı kullanıcı adı |
| `DB_PASSWORD` | Veritabanı şifresi |
| `REDIS_HOST` | Redis sunucu adresi |
| `REDIS_PORT` | Redis portu |

## 👩‍💻 Geliştirici

**Hatice Hande Dereli** — [@hanndes](https://github.com/hanndes)