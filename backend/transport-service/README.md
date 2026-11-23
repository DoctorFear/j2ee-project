# Transport Service

Service vận tải quản lý 10 bảng theo mô hình FUTA MVP trên PostgreSQL. Flyway tạo hoặc nâng cấp schema, Hibernate kiểm tra schema khi khởi động.

## Chạy local

Tạo database `futabus_transport_service` trên PostgreSQL và cấp quyền cho tài khoản kết nối. Nếu dùng Docker Compose của project, đặt `DB_URL=jdbc:postgresql://localhost:5433/futabus_transport_service` theo cổng PostgreSQL trên máy host. Có thể cấu hình kết nối trong `application-local.yml`.

| Biến môi trường     | Giá trị mặc định                                             |
| ------------------- | ------------------------------------------------------------ |
| `DB_URL`            | `jdbc:postgresql://localhost:5432/futabus_transport_service` |
| `DB_USERNAME`       | `postgres`                                                   |
| `DB_PASSWORD`       | Chuỗi rỗng                                                   |
| `FUTA_SEED_ENABLED` | `true` trong profile `local`                                 |

Chạy `./mvnw spring-boot:run` từ thư mục `transport-service`. URL gốc khi gọi trực tiếp là `http://localhost:8081/api/transport-service`. Qua Gateway, gọi `http://localhost:8080` với đường dẫn tài nguyên tương ứng (ví dụ `/trips`, `/routes`). Seed local tạo các chuyến mẫu còn thiếu theo lịch tại lần tạo đầu tiên.

### Database PostgreSQL đã có dữ liệu

Giữ URL PostgreSQL trong `DB_URL` hoặc `application-local.yml`. Xóa cấu hình ghi đè `ddl-auto: update` để Hibernate dùng `validate`. Với database được bản cũ tạo bằng Hibernate và chưa có lịch sử Flyway, sao lưu rồi đặt `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` cho lần chạy đầu tiên. Flyway đánh dấu phiên bản 1 và áp dụng [migration nâng cấp](src/main/resources/db/migration/V2__upgrade_legacy_transport_schema.sql). Bỏ biến này sau khi nâng cấp.

Migration giữ ID hiện có, đưa địa chỉ trạm cũ vào lịch sử và chuyển thứ tự điểm dừng bắt đầu từ 0 sang bắt đầu từ 1. Mốc công bố, hủy và phiên bản thống kê thiếu trong dữ liệu cũ được khởi tạo theo quy tắc ghi trong migration. Nếu dữ liệu vi phạm ràng buộc mới, PostgreSQL hủy giao dịch nâng cấp để người quản trị có thể sửa dữ liệu và chạy lại.

[Migration V3](src/main/resources/db/migration/V3__use_vietnam_local_time.sql) chuyển các cột ngày giờ từ quy ước UTC cũ sang giờ Việt Nam một lần. Migration giả định dữ liệu cũ đã lưu theo UTC.

## API

Phản hồi thành công dùng `ApiResponse` với `success`, `data` và `timestamp`. Lỗi dùng `application/problem+json` với `status`, `detail`, `errorCode` và `errors` khi có lỗi trường. Tạo mới thường trả `201` và `Location`, đọc và cập nhật trả `200`, xóa trả `204`.

Schema và API dùng giờ Việt Nam (`Asia/Ho_Chi_Minh`). Ngày giờ có định dạng ISO local datetime, ví dụ `2026-11-23T08:00:00` là 8 giờ sáng tại Việt Nam. API lọc ca trực theo ngày Việt Nam. `licenseExpiryOn` là ngày lịch, định dạng `YYYY-MM-DD`. Giá vé và các khoản tiền tính bằng VND nguyên.

### Cách gọi API

- **Qua API Gateway (cổng 8080):** Gateway tự động áp dụng filter `PrefixPath=/api/transport-service`, client gọi theo đường dẫn tài nguyên không có tiền tố `/api`. Ví dụ: `http://localhost:8080/trips`, `http://localhost:8080/routes`, `http://localhost:8080/driver/trips`.
- **Gọi trực tiếp Transport Service (cổng 8081):** Servlet context-path là `/api/transport-service`. Ví dụ: `http://localhost:8081/api/transport-service/trips`.

| Tài nguyên     | Endpoint qua Gateway (:8080)   | Endpoint trực tiếp (:8081)                         | Thao tác                                                                                                                    |
| -------------- | ------------------------------ | -------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| Trạm           | `/stations`                    | `/api/transport-service/stations`                  | CRUD trạm. `GET /stations/{id}?at=...` lấy địa chỉ hiệu lực tại thời điểm theo giờ Việt Nam.                                |
| Địa chỉ trạm   | `/stations/{id}/addresses`     | `/api/transport-service/stations/{id}/addresses`   | GET và POST danh sách địa chỉ. POST nhận `address` và `effectiveFromAt`. PUT trạm thêm bản ghi khi địa chỉ đổi.             |
| Đoạn đường mẫu | `/stations/segments`           | `/api/transport-service/stations/segments`         | CRUD đoạn đường mẫu, nhận `fromStationId`, `toStationId`, `distanceKm` và `durationMinutes`.                               |
| Tuyến          | `/routes`                      | `/api/transport-service/routes`                    | CRUD tuyến. PUT nhận các trường lúc tạo và `status` (ACTIVE, INACTIVE).                                                      |
| Điểm dừng      | `/route-stops`                 | `/api/transport-service/route-stops`               | CRUD điểm dừng trên tuyến. GET danh sách hỗ trợ lọc theo tuyến `?routeId=...`.                                              |
| Xe             | `/vehicles`                    | `/api/transport-service/vehicles`                  | POST, GET, DELETE xe. `PUT /vehicles/{id}/status` nhận `status` (ACTIVE, MAINTENANCE, INACTIVE).                            |
| Tài xế         | `/drivers`                     | `/api/transport-service/drivers`                   | CRUD tài xế. PUT nhận các trường lúc tạo và `status` (ACTIVE, INACTIVE).                                                    |
| Chuyến         | `/trips`                       | `/api/transport-service/trips`                     | POST và GET chuyến. GET, PUT và DELETE `/trips/{id}`. PUT và DELETE áp dụng cho bản nháp (DRAFT).                           |
| Phân công      | `/trip-drivers`                | `/api/transport-service/trip-drivers`              | POST, GET, DELETE phân công tài xế. POST nhận `tripId`, `driverId` và `role` (PRIMARY, RELIEF).                             |
| Thống kê vé    | `/trip-statistics`             | `/api/transport-service/trip-statistics`           | POST nhận ingest thống kê vé. GET danh sách hoặc GET theo chuyến tại `/trip-statistics/{tripId}`.                           |
| Hồ sơ tài xế   | `/driver/trips/profile`        | `/api/transport-service/driver/trips/profile`      | GET: Lấy hồ sơ tài xế đồng bộ từ Admin (hỗ trợ params `driverId`, `phone`, header `X-User-Id`).                             |
| Lịch ca trực   | `/driver/trips`                | `/api/transport-service/driver/trips`              | GET: Lịch trình ca trực tài xế (hỗ trợ lọc `date`, params `driverId`, `phone`, header `X-User-Id`).                        |
| Trạng thái xe  | `/driver/trips/{code}/status`  | `/api/transport-service/driver/trips/{code}/status`| PUT: Tài xế cập nhật trạng thái chuyến (`?status=RUNNING` hoặc `COMPLETED`).                                                |

### Lập tuyến và công bố chuyến

1. Tạo các trạm. Tạo tuyến với `code`, `versionNo` (mặc định 1), `name`, `basePrice`, `effectiveFromAt` và `effectiveToAt` tùy chọn. Các phiên bản cùng mã dùng khoảng hiệu lực `[effectiveFromAt, effectiveToAt)` riêng.
2. Tạo điểm dừng với `routeId`, `stationId`, `stopOrder`, `distanceKm`, `offsetMinutes`, `allowPickup` và `allowDropOff`. Một tuyến dùng được cần ít nhất hai điểm, thứ tự liên tục từ 1 và cự ly, phút lũy kế tăng dần từ 0. Điểm đầu đón khách và điểm cuối trả khách. Khi đã có chuyến tham chiếu, lộ trình được ghim vào phiên bản đó.
3. Tạo chuyến `DRAFT` với `code`, `routeId`, `departureAt`, `arrivalAt` và `ticketPrice`. `vehicleId` có thể trống. Giờ đến theo lịch bằng giờ đi cộng `offsetMinutes` của điểm cuối.
4. Phân xe và tài xế, sau đó gọi `POST /trips/{id}/publish`. Service kiểm tra tuyến hiệu lực, xe hoạt động, đúng một tài xế chính, tài xế hoạt động và lịch điều phối của xe, tài xế.

| Thao tác                   | Endpoint qua Gateway                                              | Dữ liệu                                      |
| -------------------------- | ----------------------------------------------------------------- | -------------------------------------------- |
| Phân hoặc đổi xe           | `PUT /trips/{id}/vehicle`                                         | `vehicleId`                                  |
| Thay tài xế chính          | `PUT /trips/{id}/primary-driver`                                  | `driverId`                                   |
| Mở, tạm dừng hoặc đóng bán | `PUT /trips/{id}/sale-status`                                     | `saleStatus` (OPEN, PAUSED, CLOSED)          |
| Cập nhật ước tính          | `PUT /trips/{id}/estimate`                                        | `estimatedDepartureAt`, `estimatedArrivalAt` |
| Xuất bến hoặc hoàn thành   | `PUT /driver/trips/{code}/status?status=RUNNING` hoặc `COMPLETED` | Tham số `status`                             |
| Hủy trước khi xuất bến     | `POST /trips/{id}/cancel`                                         | `reason`                                     |

Sau khi công bố, các mốc theo lịch được giữ cố định. Hệ thống ghi riêng giờ ước tính, giờ thực tế và thông tin hủy. Đổi xe được hỗ trợ ở bản nháp và chuyến đã công bố còn `NOT_OPEN`. Kể từ khi mở bán, sơ đồ ghế và lượt giữ chỗ của Booking cần được kiểm tra để hỗ trợ đổi xe.

### Nhận thống kê theo phiên bản

POST `/trip-statistics` nhận một bộ gồm `tripId`, `validTicketCount`, `collectedAmount`, `refundedAmount`, `sourceRevision` và `sourceAsOfAt`. Mỗi chuyến có tối đa một bản ghi, với `updatedAt` do Transport gán tại lần nhận. Lần đầu trả `201`. Các lần sau trả `200` và chỉ thay toàn bộ bản ghi khi `sourceRevision` lớn hơn bản hiện tại. API nhận số liệu từ nguồn bán, việc tự động gửi từ Booking chưa được nối vào luồng này.

Chạy `./mvnw test` để kiểm thử API với H2.
