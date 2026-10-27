# Build backend với Maven

`pom.xml` là Maven Parent (`com.futabus:futabus-backend:0.0.1-SNAPSHOT`)
cho hai module Java: `gateway` và `transport-service`.

Parent quản lý Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3,
Lombok 1.18.46, MapStruct 1.6.3, annotation processors và cấu hình đóng gói
Spring Boot. Dependency riêng của từng service được khai báo trong POM của module.

`auth-service` dùng .NET nên build riêng. `booking-service` chưa có `pom.xml`;
khi triển khai Maven cho service này, cho nó kế thừa parent và thêm vào `<modules>`.

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
```

Transport Service cần cấu hình PostgreSQL trong `application-local.yml` để chạy
với profile `local`. Các bài kiểm thử hiện có dùng H2 với profile `test`.
