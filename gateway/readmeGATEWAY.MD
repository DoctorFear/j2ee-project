# Hướng Dẫn Kỹ Thuật & Cấu Trúc API Gateway (FUTA Bus)

Tài liệu tóm tắt các bước triển khai, cấu hình định tuyến, bộ lọc an ninh và cơ chế vận hành **API Gateway (Spring Cloud Gateway)** cho hệ thống Microservices FUTA Bus.

---

## 1. Các thư viện sử dụng trong `pom.xml`

| Tên thư viện | Phiên bản | Công dụng |
| :--- | :--- | :--- |
| **spring-boot-starter-parent** | `3.3.5` | Quản lý phiên bản Spring Boot chuẩn và cấu hình build mặc định. |
| **spring-cloud-starter-gateway** | `2023.0.3` | Thư viện Spring Cloud Gateway (dựa trên Netty phi chặn) để làm Reverse Proxy, định tuyến API và xử lý CORS tập trung. |
| **spring-boot-starter-actuator** | *theo parent* | Cung cấp endpoint giám sát trạng thái sức khỏe của Gateway (`/actuator/health`). |
| **spring-boot-maven-plugin** | *theo parent* | Plugin đóng gói toàn bộ ứng dụng thành 1 file JAR thực thi độc lập (`gateway-1.0.0.jar`). |

---

## 2. Các tệp đã triển khai trong thư mục `gateway/`

| Tệp mã nguồn | Vai trò & Nội dung |
| :--- | :--- |
| [pom.xml](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/pom.xml) | Cấu hình Maven tải Spring Boot `3.3.5` và Spring Cloud Gateway `2023.0.3`, tương thích Java 21+. |
| [GatewayApplication.java](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/src/main/java/com/futa/gateway/GatewayApplication.java) | Lớp khởi chạy chính của Spring Boot (`@SpringBootApplication`). |
| [application.yml](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/src/main/resources/application.yml) | Khai báo cổng mặc định `8080`, tên ứng dụng `futa-gateway` và bật profile `local`. |
| [application-local.yml](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/src/main/resources/application-local.yml) | Cấu hình CORS toàn cục cho Frontend (`http://localhost:5173`), bảng định tuyến microservices và bộ lọc khử trùng lặp header `DedupeResponseHeader`. |
| [InternalPathBlockFilter.java](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/src/main/java/com/futa/gateway/config/InternalPathBlockFilter.java) | Bộ lọc an ninh Spring WebFlux (`WebFilter`, `@Order(-100)`): Chặn mọi truy cập trực tiếp từ bên ngoài vào `/internal/**`, trả về `403 Forbidden`. |
| [run-gateway.ps1](file:///C:/Users/ADMIN/Desktop/j2ee-project/run-gateway.ps1) | Script PowerShell tự động tìm đường dẫn JDK có sẵn trên máy và khởi chạy Gateway mà không cần cài đặt biến môi trường thủ công. |

---

## 3. Bảng định tuyến Microservices (Routing Table)

Toàn bộ client bên ngoài chỉ cần gọi duy nhất vào cổng Gateway **`http://localhost:8080`**:

| STT | Route ID | Mẫu URL (Path Predicate) | Đích chuyển tiếp (Target URI) | Service đích |
| :---: | :--- | :--- | :--- | :--- |
| **1** | `auth-service-route` | `/api/auth/**`, `/api/user/**` | `http://localhost:5001` | **Auth Service** (.NET 10 Web API - bao gồm Form Login, Google Login, Profile) |
| **2** | `booking-trip-seats-route` | `/api/trips/{code}/seats` | `http://localhost:8082` | **Booking Service** *(ưu tiên trước Trip Service)* |
| **3** | `booking-service-route` | `/api/bookings/**`, `/api/tickets/**` | `http://localhost:8082` | **Booking Service** (Spring Boot) |
| **4** | `trip-service-route` | `/api/trips/**`, `/api/stations/**`, `/api/admin/**`, `/api/driver/**` | `http://localhost:8081` | **Trip Service** (Spring Boot) |
| **5** | *Chặn nội bộ* | `/internal/**` | *Bị chặn bởi WebFilter* | Trả về `403 Forbidden` / Ngắt request ngay tại Gateway |

---

## 4. Các giải pháp kỹ thuật bảo mật & vận hành tại Gateway

### A. Khử trùng lặp Header CORS (`DedupeResponseHeader`)
- **Vấn đề**: Khi cả Gateway và Microservice upstream (như Auth Service) đều bật CORS, trình duyệt sẽ nhận 2 giá trị cho header `Access-Control-Allow-Origin: http://localhost:5173, http://localhost:5173` và từ chối request do vi phạm đặc tả CORS.
- **Giải pháp**: Cấu hình `default-filters` trong `application-local.yml`:
  ```yaml
  spring:
    cloud:
      gateway:
        default-filters:
          - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST
  ```
  Gateway sẽ tự động giữ lại giá trị hợp lệ đầu tiên và loại bỏ header trùng lặp, đảm bảo Frontend fetch dữ liệu 100% trơn tru.

### B. Chặn các Endpoint nội bộ (`InternalPathBlockFilter`)
- **Nguyên tắc**: Các endpoint `/internal/**` chỉ phục vụ giao tiếp trực tiếp giữa các microservices (Server-to-Server kèm khóa bí mật `X-Internal-Key`), tuyệt đối không được để lộ ra Internet.
- **Cơ chế**: Lớp `InternalPathBlockFilter` kế thừa `WebFilter` với `@Order(-100)`:
  - Bắt mọi request có đường dẫn bắt đầu bằng `/internal/`.
  - Phản hồi ngay lập tức mã trạng thái `403 Forbidden` kèm payload JSON:
    ```json
    {
      "success": false,
      "message": "Access to internal API is forbidden from external gateway.",
      "data": null
    }
    ```
  - Không cho phép request đi tiếp vào chuỗi định tuyến nội bộ.

---

## 5. Kết quả kiểm thử thực tế (Live Integration Test)

### A. Biên dịch và Đóng gói
```powershell
mvn -f gateway/pom.xml package -DskipTests
```
$\rightarrow$ Kết quả: **`BUILD SUCCESS`**, tạo ra tệp JAR thực thi [gateway/target/gateway-1.0.0.jar](file:///C:/Users/ADMIN/Desktop/j2ee-project/gateway/target/gateway-1.0.0.jar).

### B. Khởi động dịch vụ
Gateway khởi chạy trên Netty lắng nghe tại cổng `8080`:
```text
Netty started on port 8080 (http)
Started GatewayApplication in 4.067 seconds
```

### C. Kiểm tra định tuyến qua Gateway
Gửi yêu cầu đăng nhập từ cổng Gateway `8080`:
```powershell
POST http://localhost:8080/api/auth/login
Content-Type: application/json
Body: { "username": "admin", "password": "Admin@123" }
```

**Kết quả trả về thành công:**
```json
{
  "success": true,
  "message": "Đăng nhập thành công!",
  "data": {
    "userId": 1,
    "username": "admin",
    "email": "admin@futabus.vn",
    "fullName": "Quản trị viên FutaBus",
    "role": "Admin",
    "accessToken": "ey...",
    "refreshToken": "..."
  }
}
```
$\rightarrow$ **Minh chứng:** Gateway (8080) nhận yêu cầu $\rightarrow$ khử trùng lặp header CORS $\rightarrow$ chuyển tiếp chính xác sang Auth Service (5001) $\rightarrow$ đọc dữ liệu từ SQL Server `FutaBus_AuthDb` $\rightarrow$ trả dữ liệu về an toàn cho Frontend.

### D. Kiểm tra chặn `/internal/**`
Gửi request thử nghiệm:
```powershell
GET http://localhost:8080/internal/users
```
$\rightarrow$ **Kết quả:** `403 Forbidden` (Được ngăn chặn thành công bởi `InternalPathBlockFilter`).

---

## 6. Cách khởi chạy Gateway khi phát triển

Từ thư mục gốc `j2ee-project`, chạy 1 lệnh duy nhất:

```powershell
.\run-gateway.ps1
```

Hoặc sử dụng lệnh Maven trực tiếp:
```powershell
mvn -f gateway/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```
*(Endpoint kiểm tra sức khỏe của Gateway: `http://localhost:8080/actuator/health`)*
