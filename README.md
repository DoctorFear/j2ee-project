# Hệ thống quản lý vận tải hành khách FUTA

Website đặt vé xe đường dài và quản lý vận tải hành khách liên tỉnh. Hệ thống hỗ trợ toàn bộ quy trình từ cấu hình tuyến và trạm dừng, lập lịch chuyến, phân công phương tiện và tài xế, đặt vé và thanh toán trực tuyến, phát hành vé điện tử, đón/trả khách đến thống kê doanh thu.

Tài liệu này giới thiệu kiến trúc, phân công công việc, tổ chức mã nguồn và quy trình chạy hệ thống để các thành viên phát triển thống nhất và phục vụ đánh giá đồ án.

## 1. Công nghệ và kiến trúc

| Thành phần | Công nghệ | Trách nhiệm | Cổng local |
| --- | --- | --- | --- |
| Frontend | React, TypeScript, Vite, React Router | Giao diện cho khách hàng, quản trị/điều phối và tài xế | 5173 |
| API Gateway | Spring Cloud Gateway | Cổng API chung, định tuyến, CORS, chặn đường dẫn nội bộ | 8080 |
| Auth Service | .NET (ASP.NET Core Web API), Entity Framework Core, SQL Server | Tài khoản, vai trò, đăng nhập, đăng ký và token | 5001 |
| Transport Service | Spring Boot, JPA, PostgreSQL (hiện có Station, Route và RouteStop API) | Tuyến, trạm, chuyến, xe, tài xế, giá vé và điều phối | 8081 |
| Booking Service | Spring Boot, Spring Security, JPA, Flyway | Đơn đặt, giữ ghế, thanh toán, vé điện tử và soát vé | 8082 |
| Database | SQL Server (Auth), PostgreSQL (Trip), MySQL (Booking) | Phân tách database theo từng microservice độc lập; cổng tùy cấu hình từng máy | 1433 / 5432 / 3306 (mặc định) |

Các backend/frontend chạy trên môi trường local. Database có thể cài trực tiếp hoặc chạy trong container, tùy môi trường từng thành viên; cấu hình kết nối phải khớp database đang sử dụng. Hệ thống không sử dụng RabbitMQ. Giao tiếp giữa các service qua HTTP. Frontend gọi API qua Gateway; Auth và Booking kiểm tra JWT và quyền truy cập tại backend.

Các thư viện giao diện gồm Lucide React cho icon và qrcode.react cho mã QR. Font chữ sử dụng Plus Jakarta Sans. Booking tích hợp MoMo và gửi email xác nhận vé/thông báo hoãn chuyến; Trip tổng hợp báo cáo và xuất Excel bằng Apache POI.

Luồng kiến trúc:

```text
Frontend React :5173
        |
        v
Gateway :8080
        |-- /api/auth/** --------------------> Auth :5001 ----> SQL Server (FutaBus_AuthDb)
        |-- API tuyến, chuyến, điều phối ----> Trip :8081 ----> PostgreSQL (trip_db)
        |-- API ghế, đơn, vé, thanh toán ----> Booking :8082 -> MySQL (booking_db)

Trip <---- HTTP nội bộ ----> Booking
Trip ------ HTTP nội bộ ---> Auth
```

## 2. Phân công công việc

| Thành viên | Phần phụ trách | Nội dung |
| --- | --- | --- |
| Mai Phương | Khách hàng và đặt vé | Tìm chuyến theo chặng, lọc nâng cao, chọn ghế, giữ chỗ, thông tin đón/trả, thanh toán, danh sách vé, vé điện tử và yêu cầu hủy/hoàn theo chính sách |
| Văn Thuật | Quản trị và điều phối | Quản lý tuyến/trạm, giá vé, lập lịch chuyến, gán xe/tài xế, doanh thu và tỷ lệ lấp đầy |
| Vũ Sang | Tài xế và soát vé | Lịch chuyến được phân công, danh sách khách theo trạm, gọi khách, quét QR, cập nhật lên/xuống xe và nhật ký chuyến |
| Sơn | Xác thực và phân quyền (Auth) | Auth Service bằng .NET, phân quyền 3 vai trò (Member/Admin/Driver), Google OAuth 2.0, bảo mật JWT & Refresh Token, hồ sơ cá nhân và tích hợp Auth Frontend |

Việc chia theo người dùng không đồng nghĩa mỗi thành viên sở hữu riêng một service. Booking cung cấp dữ liệu vé/hành khách cho khách hàng, quản trị và tài xế; Trip cung cấp dữ liệu chuyến/lộ trình cho cả ba phân hệ. Các thay đổi hợp đồng dữ liệu và API cần được thống nhất giữa các phần sử dụng.

Công nghệ và kiến trúc tuân theo tài liệu `cau_truc_thu_muc_FUTA.docx`. Cả ba nhóm người dùng đăng nhập chung tại `/login`; chỉ khách hàng tự đăng ký tại `/register`. Đăng nhập Google OAuth 2.0 chính thức được tích hợp cho khách hàng (`Member`), liên kết tài khoản tự động và hỗ trợ quy trình tạo/đổi mật khẩu linh hoạt trên trang hồ sơ cá nhân.

## 3. Chức năng và phân quyền

### Xác thực và tài khoản
- Phân hệ gồm 3 vai trò: `Member` (Khách hàng), `Admin` (Quản trị viên) và `Driver` (Tài xế / Soát vé).
- Đăng ký khách hàng: Máy chủ tự gán vai trò `Member`, không nhận role từ biểu mẫu đăng ký, validate chuẩn định dạng SĐT (10 số VN) và kiểm tra email duy nhất (`IX_users_Email`).
- Đăng nhập hệ thống: Cấp Access Token JWT (15 phút) và Refresh Token (3 ngày). Tự động điều hướng theo quyền (`Admin` vào `/admin/routes`, `Driver` vào `/driver/trips`, `Member` về trang chủ `/`).
- Đăng nhập Google OAuth 2.0: Tích hợp `@react-oauth/google` và endpoint backend `POST /api/auth/google-login` xác minh token với Google API; tự động liên kết tài khoản qua email và sinh username tự động không trùng lặp (`GenerateUniqueUsernameAsync`).
- Chính sách bảo mật đăng nhập Google: Chỉ áp dụng cho vai trò `Member`. Tài khoản `Admin` và `Driver` bắt buộc phải đăng nhập bằng thông tin tài khoản nội bộ để đảm bảo an ninh vận hành.
- Cơ chế Silent Refresh Token tự động tại Frontend: Tự động gia hạn ngầm khi access token hết hạn mà không làm gián đoạn người dùng.
- Quên mật khẩu & Đặt lại mật khẩu: Gửi liên kết xác thực qua Email có thời hạn 15 phút.
- Hồ sơ cá nhân & Mật khẩu linh hoạt (`/profile`):
  - Dành riêng cho `Member` với layout và CSS cô lập (`CustomerHeader.css`).
  - Tự động nhận diện tài khoản Google chưa có mật khẩu (`hasPassword: false`) để hiển thị form **"Tạo mật khẩu"** (2 trường: mật khẩu mới và xác nhận).
  - Đối với tài khoản đã có mật khẩu (`hasPassword: true`), hiển thị form **"Đổi mật khẩu"** chuẩn (3 trường: mật khẩu hiện tại, mật khẩu mới và xác nhận).
- Tự động seed sẵn 15 tài khoản thử nghiệm khi khởi động: 5 Admin (`Admin@123`), 5 Member (`Member@123`), 5 Driver (`Driver@123`).
- Chi tiết kỹ thuật xem tại: `backend/auth-service/readmeAuth.MD`.

### Khách hàng

- Tìm chuyến theo điểm đón, điểm trả, ngày đi và ngày về; đảo chiều hành trình.
- Lọc giờ đón, loại xe, vị trí/tầng ghế, khoảng giá; sắp xếp theo giá, giờ và ghế trống.
- Xem lộ trình các trạm, địa chỉ, mốc giờ và chính sách vé.
- Chọn ghế theo cấu hình xe, tối đa 5 ghế mỗi đơn; nhập thông tin liên hệ và điểm đón/trả.
- Giữ chỗ 10 phút, thanh toán MoMo, nhận vé điện tử và email xác nhận.
- Quản lý vé sắp khởi hành/lịch sử, xem chi tiết, xuất trình QR và đặt lại tuyến.
- Hủy và hoàn toàn bộ tiền vé qua MoMo khi còn ít nhất 5 tiếng trước giờ xe khởi hành tại bến đầu tuyến. Chỉ hủy vé và trả lại ghế sau khi MoMo xác nhận hoàn tiền thành công.
- Khứ hồi tìm theo hai chiều và đặt vé từng lượt. Đón/trả tại các trạm hợp lệ của Transport; trung chuyển được bổ sung khi có danh mục khu vực phục vụ.

### Quản trị và điều phối

- Quản lý tuyến và chuỗi trạm: thứ tự, cự ly, thời gian tích lũy, địa chỉ; chèn/sửa/xóa trạm theo quy tắc.
- Tạo chuyến theo ngày/giờ từ tuyến; theo dõi chuyến, số chỗ đặt và tình trạng phân công.
- Cấu hình giá niêm yết, giảm 5–50% hoặc giá nhập tay; giá bán được tính tập trung tại Trip.
- Gán xe/tài xế có đủ điều kiện và không trùng ca; tự động gán tài xế rảnh cho chuyến sắp chạy trong 6 giờ.
- Chuyến có khách phải xử lý đổi giờ bằng quy trình delay kèm lý do và thông báo email; không xóa chuyến có khách, chỉ tạm dừng/mở bán lại.
- Báo cáo theo ngày/tuyến, doanh thu, số vé, tỷ lệ lấp đầy và xuất Excel theo bộ lọc.

### Tài xế

- Xem lịch chuyến được phân công, phương tiện, lộ trình, tiến độ đón khách và thông báo điều hành.
- Xem danh sách hành khách gom theo trạm đón/trả, tìm theo tên/ghế/điện thoại/mã vé và gọi nhanh.
- Soát QR hoặc nhập vé tay; xác nhận lên/xuống xe và xem nhật ký chuyến.
- Chỉ tài xế được phân công mới được thao tác; từ chối vé sai chuyến hoặc đã soát.

### Route giao diện

| Route | Vai trò | Nội dung |
| --- | --- | --- |
| `/login`, `/register` | Công khai | Đăng nhập chung (Form + Google OAuth), đăng ký khách hàng |
| `/forgot-password`, `/reset-password` | Công khai | Quên và đặt lại mật khẩu qua email xác thực |
| `/` | Công khai / Member | Tìm chuyến; đặt vé yêu cầu đăng nhập |
| `/trips/:code/book` | Member | Chọn ghế và thanh toán |
| `/my-tickets`, `/profile` | Member | Quản lý vé và hồ sơ cá nhân khách hàng |
| `/admin/routes` | Admin | Quản lý tuyến đường và trạm dừng |
| `/admin/trips` | Admin | Lịch chuyến, phân công xe và tài xế |
| `/admin/reports` | Admin | Báo cáo doanh thu và tỷ lệ lấp đầy |
| `/admin/drivers`, `/admin/vehicles` | Admin | Danh sách nhân sự tài xế và phương tiện |
| `/driver/trips` | Driver | Lịch các chuyến xe được phân công |
| `/driver/trips/:code/passengers` | Driver | Danh sách hành khách theo điểm đón/trả |
| `/driver/trips/:code/scan`, `/driver/trips/:code/log` | Driver | Quét mã QR soát vé và nhật ký chuyến đi |

> **Cơ chế điều hướng bảo vệ (`ProtectedRoute`)**: Không sử dụng trang 403 trung gian gây gián đoạn trải nghiệm người dùng. Khi truy cập vào route không thuộc quyền hạn, hệ thống tự động nhận diện vai trò và điều hướng thẳng về khu vực tương ứng của người dùng (`/admin/routes` cho Admin, `/driver/trips` cho Driver, hoặc `/` cho Member). Nếu chưa đăng nhập, tự động chuyển về `/login` kèm lưu lại trang đang truy cập để quay lại sau khi đăng nhập thành công.

## 4. Cấu trúc thư mục

```text
j2ee-project/
├── README.md
├── frontend/
│   └── src/
│       ├── app/                Khởi tạo, layout, guard, route và provider
│       ├── api/                HTTP client và xử lý phiên dùng chung
│       ├── features/
│       │   ├── auth/           Đăng nhập, đăng ký và trạng thái phiên
│       │   ├── customer/       Tìm chuyến, đặt chỗ và vé khách hàng
│       │   ├── admin/          Quản trị và điều phối
│       │   ├── account/        Hồ sơ cá nhân
│       │   └── driver/         Trang tài xế
│       ├── components/         Thành phần dùng chung
│       ├── lib/                Tiện ích, định dạng và biến môi trường
│       └── styles/             CSS toàn cục và CSS quản trị
└── backend/
    ├── pom.xml                 Maven parent cho các service Spring Boot
    ├── auth-service/           .NET Auth Service và EF migrations
    ├── transport-service/      Tuyến, trạm, chuyến, xe và tài xế
    ├── booking-service/        Giữ ghế, đặt vé, thanh toán và vé điện tử
    ├── gateway/                Gateway, CORS và định tuyến API
    ├── infra/                  Tài nguyên hạ tầng dùng chung
    └── postman/                Bộ sưu tập kiểm tra API
```

Đây là cấu trúc tổ chức của hệ thống; các thành viên đặt mã nguồn vào đúng module và tầng tương ứng khi bổ sung chức năng. Không gộp controller, logic nghiệp vụ và truy cập dữ liệu vào một file.

### Kiểu dữ liệu frontend

| Tệp dùng chung trong `frontend/src/features/customer/types/` | Nội dung | Phân hệ sử dụng |
| --- | --- | --- |
| `station.ts` | `Station` | Khách hàng, quản trị, tài xế |
| `route-stop.ts` | `Station` | Tuyến, chuyến và lộ trình |
| `seat.ts` | `Seat` | Đặt chỗ, sơ đồ hành khách, soát vé |
| `vehicle.ts` | `VehicleType` | Phương tiện và chuyến |
| `pricing.ts` | `Pricing` | Quản trị giá và bán vé |
| `trip.ts` | `Trip` | Cả ba phân hệ |
| `contact.ts` | `Contact` | Thông tin người đặt/hành khách |
| `ticket.ts` | `Ticket` | Vé, danh sách hành khách và soát vé |

Kiểu riêng cho khách hàng nằm tại `frontend/src/features/customer/types/`: `search.ts` chứa `SearchCriteria`, `booking.ts` chứa `BookingDraft`. Import trực tiếp từ tệp nghiệp vụ, không tạo một tệp `customer.ts` để tập hợp tất cả kiểu. Khi thêm DTO hoặc thực thể dùng chung, đặt tệp tương ứng trong `src/types/`; kiểu chỉ phục vụ một feature đặt trong `features/<feature>/types/`.

## 5. Database và quy tắc dữ liệu

| Database | Service sở hữu | Hệ quản trị CSDL | Nhóm dữ liệu |
| --- | --- | --- | --- |
| `FutaBus_AuthDb` | Auth | SQL Server | Tài khoản `users`, vai trò và refresh token |
| `trip_db` | Trip | PostgreSQL | Địa chỉ, trạm, tuyến, chuyến, xe, tài xế, delay và thông báo |
| `booking_db` | Booking | MySQL | Đơn đặt, giữ ghế, thanh toán và vé |

> **Ghi chú kiến trúc**: Áp dụng nguyên lý **Polyglot Persistence** trong Microservices: Auth dùng SQL Server, Trip dùng PostgreSQL và Booking dùng MySQL. Các service giao tiếp với nhau qua HTTP API và xác thực token JWT, hoàn toàn không phụ thuộc chéo vào hệ quản trị cơ sở dữ liệu của nhau.

Tổng số bảng được thiết kế theo phạm vi khoảng 15–20 bảng của đồ án; số lượng cụ thể phải thống nhất với mô hình dữ liệu và migrations. Không tạo FK chéo database. Các service liên kết bằng mã/ID như `user_id`, `trip_code`, `station_id`.

- `distanceKm` và `offsetMinutes` của trạm là giá trị tích lũy từ đầu tuyến; điểm đón phải đứng trước điểm trả.
- Giờ đón/trả tính bằng giờ xuất bến cộng offset; lưu ngày giờ đầy đủ, có múi giờ, để xử lý hành trình qua ngày.
- Giá do Trip tính; Booking xác minh và lưu giá tại lúc đặt, không tin giá gửi từ giao diện.
- Giữ ghế và phát hành vé phải chống trùng; theo cấu trúc đã chốt, ghế khóa theo `trip_code + seat_code` cho toàn chuyến.
- MoMo callback xử lý idempotent, kiểm tra chữ ký, số tiền và mã đơn; chỉ phát hành vé sau khi xác nhận thanh toán thành công.
- Trạng thái thanh toán/vé tách khỏi trạng thái lên/xuống xe.
- Occupancy = tổng vé thực bán / tổng sức chứa × 100%; từ 80% là Cao, từ 50% đến dưới 80% là Trung bình, dưới 50% là Thấp.
- Auth dùng EF migrations. Trip hiện dùng `ddl-auto=update` khi phát triển Station; trước khi triển khai production cần chuyển sang migration có phiên bản.

Booking đọc `ticketPrice` của Transport và áp dụng nguyên giá toàn tuyến cho các chặng đón/trả hợp lệ. Cấu hình mã ghế theo từng `vehicleId` nằm trong `booking.vehicle-seats`; số ghế phải khớp sức chứa xe và `capacitySnapshot`. Chỉ chuyến `SCHEDULED` và `OPEN`, tuyến/xe đang hoạt động và sơ đồ hợp lệ mới được mở bán.

## 6. Chuẩn bị môi trường

- Git, Node.js và npm để phát triển frontend. Frontend đã được kiểm tra với Node.js 24 và npm 11.
- .NET SDK 10 và công cụ Entity Framework Core 10 tương thích cho Auth.
- JDK 21, cấu hình `JAVA_HOME` đúng JDK. Mỗi service Java có Maven Wrapper; không bắt buộc cài Maven riêng.
- SQL Server cho Auth, PostgreSQL cho Transport và MySQL cho Booking; mỗi service cần tài khoản kết nối đúng database. Docker là lựa chọn triển khai database, không phải điều kiện bắt buộc.
- Thông tin merchant MoMo, IPN công khai và SMTP để chạy thanh toán/gửi email.
- Microsoft Edge nếu sử dụng script kiểm tra giao diện Playwright trong repository.

Kiểm tra các công cụ sử dụng trên máy; Maven và CLI database không bắt buộc nếu dùng Wrapper hoặc công cụ quản lý tương ứng:

```powershell
node --version
npm --version
dotnet --version
java -version
mvn -version
mysql --version
psql --version
```

### Điều kiện chạy backend

Các database phải sẵn sàng và thông tin kết nối trong cấu hình local phải đúng môi trường từng thành viên. Auth cần áp dụng EF migrations trước lần chạy trên database mới. Transport và Booking hiện mặc định chọn profile `local` khi không chỉ định profile khác. Thanh toán cần thông tin merchant MoMo và IPN công khai; email cần cấu hình SMTP hợp lệ.

## 7. Cấu hình local

| Thành phần | Tệp cấu hình | Giá trị cần khai báo |
| --- | --- | --- |
| Frontend | `frontend/.env.development` | `VITE_API_BASE_URL=http://localhost:8080` |
| Auth | `backend/auth-service/src/AuthService.Api/appsettings.json` | Kết nối `FutaBus_AuthDb` (SQL Server), JWT Secret, SMTP Gmail, URL Frontend |
| Trip | `backend/transport-service/src/main/resources/application-local.yml` | Cổng API 8081, kết nối PostgreSQL `trip_db` theo máy; mẫu `application.example.yaml` |
| Booking | `backend/booking-service/src/main/resources/application-local.yml` | MySQL `booking_db`, JWT secret/issuer/audience khớp Auth, URL Transport, sơ đồ ghế từng xe, khóa nội bộ, MoMo và SMTP |
| Gateway | `backend/gateway/src/main/resources/application.yaml` | Cổng 8080, định tuyến service và CORS cho frontend cổng 5173 |

Tên các trường cấu hình backend phải khớp với lớp cấu hình của service. Không commit mật khẩu, token, secret MoMo, khóa RSA riêng hoặc tệp cấu hình local. Các tệp example chỉ chứa tên trường và giá trị không nhạy cảm.

Tạo cấu hình frontend từ thư mục gốc:

```powershell
if (!(Test-Path frontend/.env.development)) { Copy-Item frontend/.env.example frontend/.env.development }
```

Nếu database chưa tồn tại, tạo `trip_db` trong PostgreSQL cho Transport Service:

```sql
CREATE DATABASE trip_db;
```

Nếu `booking_db` chưa có, tạo trong MySQL cho Booking Service:

```sql
CREATE DATABASE IF NOT EXISTS booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Auth tạo database/bảng khi chạy `dotnet ef database update`; khởi động ứng dụng thực hiện seed tài khoản sau khi schema đã có.

Cấp quyền theo database cho tài khoản service tương ứng và khai báo kết nối trong cấu hình local. SQL khởi tạo/seed dùng chung đặt tại `backend/infra/mysql/`; thay đổi schema của từng service quản lý qua migration của service đó.

Auth hiện ký JWT bằng HS256. Booking xác minh cùng secret trong `JwtSettings:Secret`, issuer `FutaBusAuthApi`, audience `FutaBusClient` và claim `role`. Không tự tạo khóa khác cho Booking; không đưa secret vào frontend hoặc Git.

## 8. Chạy toàn hệ thống

Thứ tự: **Database (SQL Server, PostgreSQL, MySQL) → Auth → Trip và Booking → Gateway → Frontend**. Chạy các service ở terminal riêng. Các đường dẫn trong các lệnh này tính từ thư mục gốc repository.

### Auth Service (.NET - Cổng 5001)

Có 2 cách chạy:
- **Cách 1 (Bằng Visual Studio - Khuyên dùng)**: Mở tệp `backend/auth-service/AuthService.sln` bằng Visual Studio và nhấn nút **Play (http)** hoặc `Ctrl + F5`. Trình duyệt sẽ tự động mở Swagger UI tại `http://localhost:5001/swagger`.
- **Cách 2 (Bằng Terminal)**:
```powershell
dotnet ef database update --project backend/auth-service/src/AuthService.Api
dotnet run --project backend/auth-service/src/AuthService.Api --urls "http://localhost:5001"
```
*(Chi tiết cấu trúc, danh sách DTOs, endpoint Google Auth, câu lệnh CLI và 15 tài khoản seed sẵn cho 3 vai trò xem tại: `backend/auth-service/readmeAuth.MD`).*

### Transport Service

```powershell
mvn -f backend/transport-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Transport kết nối PostgreSQL `trip_db`; schema được Hibernate cập nhật trong môi trường phát triển. Thông tin kết nối nằm trong `backend/transport-service/src/main/resources/application-local.yml`.

### Booking Service

Tạo cấu hình từ mẫu nếu chưa có, thực hiện từ thư mục gốc repository:

```powershell
if (!(Test-Path backend/booking-service/src/main/resources/application-local.yml)) { Copy-Item backend/booking-service/src/main/resources/application-local.example.yml backend/booking-service/src/main/resources/application-local.yml }
```

Điền cấu hình local trước khi chạy:

- `spring.datasource`: tài khoản MySQL có quyền trên `booking_db`; Flyway tự tạo bảng khi khởi động, không cần chạy thủ công SQL bảng.
- `booking.jwt-secret`: trùng `JwtSettings:Secret` của Auth, ít nhất 32 byte.
- `booking.transport-url`: `http://localhost:8081`; `transport-zone: UTC` theo quy ước dữ liệu Transport. Đổi thành `Asia/Ho_Chi_Minh` nếu dữ liệu `LocalDateTime` của nhóm được nhập theo giờ Việt Nam.
- `booking.vehicle-seats`: khóa là ID xe thực tế, giá trị là danh sách mã ghế theo đúng thứ tự trên xe, ví dụ `"1": [A01, A02, B01, B02]` cho xe 4 chỗ. Với xe 34 chỗ, khai báo đủ các mã ghế thực tế. Để trống hoặc lệch sức chứa thì chuyến không mở bán.
- `booking.momo`: tài khoản merchant, access key, secret key và IPN URL công khai `https://<host>/api/bookings/payments/momo/ipn`. Endpoint mặc định là MoMo sandbox; cấu hình endpoint tương ứng tài khoản sử dụng.
- `spring.mail` và `booking.email-from`: tài khoản SMTP gửi vé; Gmail dùng mật khẩu ứng dụng. Email có mã vé, thông tin đón và ảnh QR đính kèm; gửi lỗi sẽ được thử lại.
- `booking.internal-key`: khóa riêng dùng giữa các backend; không đưa khóa này vào biến `VITE_*`.

Chạy Booking từ thư mục gốc repository:

```powershell
mvn -f backend/booking-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Có thể dùng Maven Wrapper trong từng service nếu không cài Maven riêng. Booking chạy tại `http://localhost:8082`. Frontend dùng `VITE_API_BASE_URL=http://localhost:8080` để gọi qua Gateway. Tiền và trạng thái thành công lấy từ backend; frontend không tự xác nhận thanh toán. Giao dịch về sau khi hết hạn/nhả ghế chuyển sang `PAYMENT_REVIEW` để đối soát và không phát vé trùng ghế.

| API Booking qua Gateway | Quyền và chức năng |
| --- | --- |
| `GET /api/bookings/catalog/stations`, `GET /api/bookings/catalog/trips`, `GET /api/bookings/catalog/trips/{code}` | Công khai, danh mục trạm/chuyến từ Transport |
| `POST /api/bookings/hold` | Member, giữ tối đa 5 ghế, tối đa 600 giây và không vượt giờ đón |
| `GET /api/bookings/{id}`, `DELETE /api/bookings/{id}/hold` | Chủ đơn tra cứu/nhả ghế chưa thanh toán |
| `POST /api/bookings/{id}/pay` | Chủ đơn tạo hoặc lấy lại phiên MoMo |
| `POST /api/bookings/payments/momo/ipn` | MoMo callback, bắt buộc xác minh chữ ký và số tiền |
| `GET /api/tickets/my`, `GET /api/tickets/{code}`, `GET /api/tickets/{code}/qr` | Chủ vé xem danh sách, thông tin và mã QR |
| `POST /api/tickets/{code}/cancel`, `GET /api/tickets/{code}/refund` | Chủ vé yêu cầu hủy/tra cứu kết quả hoàn tiền |

Các đơn và vé được kiểm tra chủ sở hữu bằng `sub` trong JWT; mã người dùng, giá và trạng thái thanh toán không lấy từ dữ liệu khách gửi.

### API Gateway (Cổng 8080)

Chạy qua Maven:
```powershell
mvn -f backend/gateway/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Gateway tích hợp:
- Bộ lọc `InternalPathBlockFilter` tự động chặn mọi truy cập từ bên ngoài vào `/internal/**` với mã `403 Forbidden`.
- Bộ lọc CORS deduplication `DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST` ngăn trùng lặp header CORS giữa Gateway và microservices.
- Định tuyến `/api/bookings/**`, `/api/tickets/**` và `/api/trips/*/seats` tới Booking; các API vận tải tới Transport.
  *(Chi tiết cấu hình Gateway: `backend/gateway/src/main/resources/application.yaml`).*

### Frontend

```powershell
cd frontend
npm ci
npm run dev
```

Truy cập [http://localhost:5173](http://localhost:5173). API base URL lấy từ `.env.development`; khởi động lại Vite sau khi đổi biến môi trường. Trước khi kiểm tra luồng end-to-end, các feature phải được nối với API, Auth và dữ liệu database của service tương ứng.

Các trang khách hàng gọi `src/features/customer/api/bookingApi.ts` qua HTTP client dùng chung `src/api/client.ts`: tìm chuyến từ Transport thông qua Booking, giữ ghế, thanh toán và quản lý vé. Sơ đồ ghế và trạng thái vé lấy từ backend; tải lại trang thanh toán sẽ khôi phục phiên giữ chỗ còn hiệu lực.

### Dữ liệu khởi tạo local

- Auth sử dụng `Data/Seed/DatabaseSeeder.cs` hiện có để tạo tài khoản/vai trò.
- Transport sử dụng `seed/DatabaseSeeder.java`: thêm 20 chuyến theo ngày Việt Nam, tuyến/trạm, phương tiện và tài xế; gồm giường nằm 34 chỗ, Limousine 22 chỗ và ghế ngồi 45 chỗ. Chuyến có đủ bốn khung giờ và các trạng thái mở bán, tạm dừng, đóng, nháp, đang chạy, hoàn thành, hủy.
- Booking sử dụng `seed/DatabaseSeeder.java`: chờ Transport sẵn sàng, bổ sung sơ đồ cho xe seed và tạo 5 booking / 13 vé cho tài khoản cấu hình tại `booking.seed.user-id`. Điền ID thực tế của tài khoản khách hàng trong Auth; có thể sử dụng tài khoản `member1` do Auth khởi tạo. Thông tin liên hệ có thể đặt tại `booking.seed.name`, `phone`, `email`.
- Bộ dữ liệu dùng mã `SEED-<yyyyMMdd>-...`; chạy lại cùng ngày không thêm trùng hoặc ghi đè dữ liệu đã tồn tại. Khởi động Transport ngày mới bổ sung bộ chuyến ngày mới; các bộ cũ được giữ nguyên. Booking chọn bộ seed mới nhất khi khởi động.
- Lịch kiểm tra chính: TP. Hồ Chí Minh → Bình Định hôm nay có chuyến sau khoảng 2 và 7 tiếng tính từ lần seed đầu; ngày mai có 6 chuyến mở bán theo chiều đi và 2 chuyến chiều về. Các tuyến Nha Trang, Đà Lạt, Đà Nẵng nằm ở hai ngày tới. Ngày/giờ đã lưu không tự kéo dài khi khởi động lại.
- `member1` / `Member@123` có 8 vé sắp đi, 3 vé chuyến hoàn thành và 2 vé đã hủy/hoàn ngay sau lần seed đầu. Mỗi ghế là một vé, một booking có thể chứa nhiều vé.
- Vé seed không tạo email hoặc giao dịch MoMo. Không gửi yêu cầu hoàn tiền của vé seed sang MoMo; dùng vé mới thanh toán thật để kiểm tra hoàn tiền qua cổng. Hai vé đã hoàn được tạo sẵn để xem giao diện lịch sử/trạng thái hoàn tiền.
- Seeder Java chỉ chạy ở profile `local`. Tắt bằng `futa.seed.enabled=false` ở Transport và `booking.seed.enabled=false` ở Booking. Khi tắt seeder Booking, cấu hình sơ đồ các xe cần mở bán trong `booking.vehicle-seats`.

## 9. Build và kiểm tra

### Frontend

Trong thư mục `frontend/`:

```powershell
npm run build
npm test
npm run preview
```

`build` kiểm tra TypeScript và tạo bundle trong `dist/`; `test` chạy Vitest; `preview` phục vụ bundle đã build để kiểm tra local, không thay thế cấu hình triển khai production.

Sau khi bật Vite, chạy từ thư mục gốc để kiểm tra thao tác, giới hạn ghế, vị trí popup và bố cục desktop/mobile:

```powershell
node frontend/scripts/check-ui.mjs
```

Script sử dụng Edge headless và đọc các trang tham chiếu theo đường dẫn khai báo trong script; chỉnh đường dẫn phù hợp máy nếu cần. Ảnh kiểm tra lưu trong `screenshots/` và không đưa vào Git.

### Backend

Sau khi các project backend đáp ứng điều kiện ở mục 6:

```powershell
dotnet build backend/auth-service/src/AuthService.Api
mvn -f backend/transport-service/pom.xml verify
mvn -f backend/booking-service/pom.xml verify
mvn -f backend/gateway/pom.xml verify
```

Bổ sung project test cho Auth và chạy `dotnet test` với đường dẫn solution/project test thực tế. Kiểm tra cả quyền truy cập và các tình huống nghiệp vụ: giữ trùng ghế, hết hạn giữ chỗ, callback trùng/lệch số tiền, delay chuyến có khách và soát vé sai chuyến.

## 10. Giao tiếp giữa các service

Mọi API nội bộ kèm `X-Internal-Key`; service nhận xác minh khóa, Gateway không định tuyến các đường dẫn này.

| Luồng | Endpoint nội bộ | Mục đích |
| --- | --- | --- |
| Trip → Auth | `POST /internal/users` | Tạo tài khoản tài xế và liên kết `user_id` |
| Booking → Trip | `GET /internal/trips/{code}` | Lấy tuyến, giờ, giá, sơ đồ xe và tài xế được phân công |
| Trip → Booking | `POST /internal/bookings/booked-counts` | Lấy số ghế đã đặt theo danh sách chuyến |
| Trip → Booking | `GET /internal/reports/trip-sales` | Tổng hợp vé bán và doanh thu |
| Trip → Booking | `GET /internal/trips/{code}/stop-counts` | Số khách đón/trả theo trạm |
| Trip → Booking | `POST /internal/bookings/trip-delayed` | Gửi thông báo hoãn chuyến tới hành khách |

Các endpoint trên là hợp đồng kiến trúc; controller, DTO, validation và xử lý lỗi phải thống nhất với service gọi. Thống nhất mã lỗi nghiệp vụ như `SEAT_UNAVAILABLE`, `HOLD_EXPIRED`, `INVALID_SEGMENT`, `PRICE_CHANGED`, `FORBIDDEN` để frontend xử lý nhất quán.

Booking đã cung cấp `POST /internal/bookings/booked-counts` với body danh sách mã chuyến, `GET /internal/reports/trip-sales?tripCodes=T001,T002` và `GET /internal/trips/{code}/stop-counts`. Cả ba trả envelope `{success,data}` và bắt buộc `X-Internal-Key`. Báo cáo tách doanh thu đã thu, số tiền đã hoàn và doanh thu ròng. Transport gọi trực tiếp cổng 8082; Gateway chặn `/internal/**`. Những endpoint nội bộ khác trong bảng cần phối hợp với phần phụ trách tương ứng trước khi sử dụng.

## 11. Nguyên tắc phát triển

1. Tuân thủ cấu trúc thư mục và trách nhiệm từng tầng. Trang ghép component; hook xử lý trạng thái; API module gọi server; controller nhận/trả HTTP; service xử lý nghiệp vụ; repository truy cập dữ liệu.
2. Tách kiểu dùng chung theo thực thể/nghiệp vụ để các thành viên dễ tìm và mở rộng. Chỉ gộp các kiểu ngắn, liên quan và chỉ dùng trong một feature.
3. Giữ nguyên theme, bố cục, font chữ và icon đã thống nhất. Ưu tiên Lucide để đồng bộ icon.
4. Chỉ thay đổi mã liên quan yêu cầu; xem xét phụ thuộc và kiểm tra tác động về kỹ thuật, logic, giao diện trước khi bàn giao.
5. Mỗi tệp mã nguồn có comment đầu tệp và các hàm/đoạn quan trọng có comment ngắn tiếng Việt dạng `// Chức năng`. Comment chỉ mô tả chức năng, không ghi lịch sử chỉnh sửa hoặc giải thích từng dòng. CSS/HTML dùng cú pháp comment hợp lệ; JSON không chèn comment.
6. Không commit secret và cấu hình local. Schema thay đổi qua migration; kiểm tra role ở backend; các API vé kiểm tra chủ sở hữu và các API tài xế kiểm tra chuyến được phân công.
7. Khi đổi hợp đồng API/type, cập nhật tất cả nơi sử dụng và kiểm tra build/test liên quan trước khi ghép mã.

## 12. Xử lý lỗi môi trường

| Hiện tượng | Kiểm tra |
| --- | --- |
| Frontend không gọi được API | Gateway đã chạy, đúng `VITE_API_BASE_URL`, CORS và route tới service |
| API trả 401/403 | Hạn token, JWT secret HS256 khớp Auth, issuer/audience, role và quyền sở hữu dữ liệu |
| Service không kết nối database | Kiểm tra đúng PostgreSQL cho Trip hoặc MySQL cho Booking, user/password, quyền, port và cấu hình local |
| Spring không nạp cấu hình local | Profile `local` đã bật, tệp `application-local.yml` đúng thư mục |
| Auth không đọc cấu hình | Environment `Development`, connection string SQL Server và JWT secret |
| Migration thất bại | Phiên bản migration, quyền database và dữ liệu hiện có; không xóa database để xử lý tùy tiện |
| MoMo không báo kết quả | Cấu hình provider, chữ ký, mã đơn/số tiền và khả năng truy cập IPN URL từ bên ngoài |
| Cổng đã được sử dụng | Xác định tiến trình dùng cổng và thống nhất cổng trong cấu hình các service |
