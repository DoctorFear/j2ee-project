# Backend FUTA

## Chạy backend bằng Docker Compose

Chạy các lệnh sau từ thư mục gốc repo. Máy cần Docker và Docker Compose. `.env.backend.local` chứa cấu hình cho máy local và đã được Git bỏ qua.

```bash
cp .env.backend.example .env.backend.local  # Chỉ tạo ở lần đầu
docker compose --env-file .env.backend.local -f compose.backend.yml up -d --build
docker compose --env-file .env.backend.local -f compose.backend.yml ps
```

Gateway nhận request tại `http://localhost:8080`. Auth và Transport dùng hai database riêng trên cùng PostgreSQL. Booking dùng MySQL. Ba service này tự áp dụng migration khi khởi động. PostgreSQL và MySQL lưu dữ liệu trong volume riêng của stack `futa-backend`.

```bash
curl http://localhost:8080/trips
curl http://localhost:8080/bookings/catalog/stations
```

Đăng nhập tại `POST /auth/login` bằng tài khoản mẫu `member1` / `Member@123`. `BOOKING_SEED_USER_ID=6` là ID của `member1` trong database Auth được khởi tạo mới. Nếu dùng dữ liệu Auth khác, điền ID thực tế của tài khoản mẫu hoặc đặt `BOOKING_SEED_ENABLED=false`.

Khi chạy frontend trên máy, dùng `VITE_API_BASE_URL=http://localhost:8080`. Nếu cổng 8080 bận, đổi `GATEWAY_PORT` trong `.env.backend.local` và cập nhật `VITE_API_BASE_URL` theo cổng mới. Để kiểm thử thanh toán hoặc email, điền các biến `PAYOS_*` và `SMTP_*` trong `.env.backend.local`. PayOS có thể gọi đường dẫn `/bookings/payments/payos/webhook` qua Gateway khi đã thiết lập HTTPS công khai.

```bash
docker compose --env-file .env.backend.local -f compose.backend.yml logs -f
docker compose --env-file .env.backend.local -f compose.backend.yml down
```

`down` giữ dữ liệu database để lần chạy sau sử dụng lại.

## Build backend với Maven

`pom.xml` là Maven Parent (`com.futabus:futabus-backend:0.0.1-SNAPSHOT`)
cho ba module Java: `gateway`, `transport-service` và `booking-service`.

Parent quản lý Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3,
Lombok 1.18.46, MapStruct 1.6.3, annotation processors và cấu hình đóng gói
Spring Boot. Dependency riêng của từng service được khai báo trong POM của module.

`auth-service` dùng .NET nên build riêng. `booking-service` kế thừa Maven Parent
và được khai báo trong `<modules>`.

Yêu cầu JDK 21 trở lên và Maven 3.6.3 trở lên. Chạy các lệnh sau từ thư mục `backend`:

```powershell
mvn clean verify
```

Build một module và các module phụ thuộc:

```powershell
mvn -pl transport-service -am clean verify
```

Chạy từng service trong terminal riêng:

```powershell
mvn -pl gateway spring-boot:run
mvn -pl transport-service spring-boot:run "-Dspring-boot.run.profiles=local"
mvn -pl booking-service spring-boot:run "-Dspring-boot.run.profiles=local"
```

Transport Service dùng PostgreSQL, database `futabus_transport_service` và Flyway.
Xem cách kết nối và nâng cấp schema PostgreSQL trong [README của Transport](transport-service/README.md). Các bài kiểm thử API dùng H2 với profile `test`.
Booking Service cần cấu hình MySQL trong `application-local.yml` để chạy với
profile `local`.
