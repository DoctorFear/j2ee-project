# Hệ thống quản lý vận tải hành khách FUTA

Website đặt vé xe đường dài và quản lý vận tải hành khách liên tỉnh. Hệ thống hỗ trợ toàn bộ quy trình từ cấu hình tuyến và trạm dừng, lập lịch chuyến, phân công phương tiện và tài xế, đặt vé và thanh toán trực tuyến, phát hành vé điện tử, đón/trả khách đến thống kê doanh thu.

Tài liệu này giới thiệu kiến trúc, phân công công việc, tổ chức mã nguồn và quy trình chạy hệ thống để các thành viên phát triển thống nhất và phục vụ đánh giá đồ án.

## 1. Công nghệ và kiến trúc

| Thành phần | Công nghệ | Trách nhiệm | Cổng local |
| --- | --- | --- | --- |
| Frontend | React, TypeScript, Vite, React Router | Giao diện cho khách hàng, quản trị/điều phối và tài xế | 5173 |
| API Gateway | Spring Cloud Gateway | Cổng API chung, định tuyến, CORS, xác minh JWT | 8080 |
| Auth Service | .NET (ASP.NET Core Web API), Entity Framework Core, SQL Server | Tài khoản, vai trò, đăng nhập, đăng ký và token | 5001 |
| Trip Service | Spring Boot, JPA, PostgreSQL (hiện có Station API) | Tuyến, trạm, chuyến, xe, tài xế, giá vé và điều phối | 8081 |
| Booking Service | Spring Boot, Spring Security, JPA, Flyway | Đơn đặt, giữ ghế, thanh toán, vé điện tử và soát vé | 8082 |
| Database | SQL Server (Auth), PostgreSQL (Trip), MySQL (Booking) | Phân tách database theo từng microservice độc lập | 1433 / 5432 / 3306 |

Các service chạy trực tiếp trên môi trường local, không sử dụng Docker hoặc RabbitMQ. Giao tiếp giữa các service qua HTTP. Frontend gọi API qua Gateway; các service kiểm tra JWT và quyền truy cập tại backend.

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
- Chi tiết kỹ thuật xem tại: `auth-service/readmeAuth.MD`.

### Khách hàng

- Tìm chuyến theo điểm đón, điểm trả, ngày đi và ngày về; đảo chiều hành trình.
- Lọc giờ đón, loại xe, vị trí/tầng ghế, khoảng giá; sắp xếp theo giá, giờ và ghế trống.
- Xem lộ trình các trạm, địa chỉ, mốc giờ và chính sách vé.
- Chọn ghế theo cấu hình xe, tối đa 5 ghế mỗi đơn; nhập thông tin liên hệ và điểm đón/trả.
- Giữ chỗ 10 phút, thanh toán MoMo, nhận vé điện tử và email xác nhận.
- Quản lý vé sắp khởi hành/lịch sử, xem chi tiết, xuất trình QR và đặt lại tuyến.
- Hủy/hoàn tiền, trung chuyển và khứ hồi cần tuân thủ quy trình và chính sách được thống nhất trước khi triển khai API.

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
├── docs/                       Tài liệu nghiệp vụ và hợp đồng dữ liệu
├── infra/
│   ├── mysql/                  SQL khởi tạo database và dữ liệu
│   └── keys/                   Hướng dẫn và khóa công khai RSA
├── scripts/                    Script vận hành các service
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   ├── .env.example
│   ├── scripts/                Kiểm tra giao diện bằng trình duyệt
│   └── src/
│       ├── main.tsx            Khởi tạo React
│       ├── App.tsx             Ghép route của ứng dụng
│       ├── routes/             Khai báo route và kiểm tra quyền
│       ├── auth/               Trạng thái đăng nhập, token và phiên
│       ├── api/                HTTP client và API theo nghiệp vụ
│       ├── layouts/            Layout các nhóm người dùng
│       ├── features/
│       │   ├── auth/           Trang đăng nhập và đăng ký
│       │   ├── customer/       Trang khách hàng, component và types cục bộ
│       │   ├── admin/          Trang quản trị và điều phối
│       │   └── driver/         Trang tài xế
│       ├── components/         Thành phần dùng chung
│       ├── hooks/              Logic trạng thái có thể tái sử dụng
│       ├── types/              Kiểu dữ liệu dùng chung theo nghiệp vụ
│       ├── constants/          Hằng số và trạng thái nghiệp vụ
│       ├── data/               Danh mục và dữ liệu khởi tạo
│       └── utils/              Định dạng, tính toán và chuẩn hóa
├── auth-service/
│   └── src/AuthService.Api/
│       ├── Controllers/        HTTP endpoint
│       ├── Services/           Xác thực, token và hồ sơ
│       ├── Repositories/       Truy cập dữ liệu
│       ├── Data/
│       │   ├── Migrations/     EF migrations
│       │   └── Seed/           Vai trò và tài khoản khởi tạo
│       ├── Entities/           Thực thể tài khoản và refresh token
│       ├── Dtos/               Dữ liệu request/response
│       ├── Security/           Kiểm tra quyền và khóa nội bộ
│       └── Keys/               Khóa RSA của Auth
├── trip-service/
│   └── src/main/
│       ├── java/futabus/trip_service/
│       │   ├── controller/     Station API và xử lý lỗi HTTP
│       │   ├── service/        Logic Station
│       │   ├── repository/     Truy cập dữ liệu Trip
│       │   ├── entity/         Thực thể vận tải
│       │   ├── dto/            request/ và response/
│       │   ├── mapper/         Chuyển entity và DTO
│       │   └── enums/          Loại và trạng thái trạm
│       └── resources/          Cấu hình PostgreSQL (file local và example)
├── booking-service/
│   └── src/main/
│       ├── java/com/futa/booking/
│       │   ├── controller/     API giữ ghế, đơn, thanh toán, vé và soát vé
│       │   ├── service/        Logic Booking
│       │   ├── repository/     Truy cập dữ liệu Booking
│       │   ├── entity/         Đơn, ghế giữ, thanh toán và vé
│       │   ├── dto/            Request/response
│       │   ├── client/         Gọi Trip
│       │   ├── config/         MoMo, email và lịch tác vụ
│       │   ├── security/       JWT và xác thực nội bộ
│       │   ├── scheduler/      Dọn giữ chỗ hết hạn
│       │   └── exception/      Xử lý lỗi nghiệp vụ
│       └── resources/db/migration/  Flyway migrations
└── gateway/
    └── src/main/
        ├── java/com/futa/gateway/config/  Định tuyến và bảo mật
        └── resources/          Cấu hình Gateway
```

Đây là cấu trúc tổ chức của hệ thống; các thành viên đặt mã nguồn vào đúng module và tầng tương ứng khi bổ sung chức năng. Không gộp controller, logic nghiệp vụ và truy cập dữ liệu vào một file.

### Kiểu dữ liệu frontend

| Tệp dùng chung trong `frontend/src/types/` | Nội dung | Phân hệ sử dụng |
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

Chi tiết hợp đồng dữ liệu đặt vé tại [docs/customer-data-model.md](docs/customer-data-model.md). Các quy tắc giá nhập tay theo chặng, khứ hồi, trung chuyển và hủy/hoàn phải được thống nhất giữa các service trước khi triển khai.

## 6. Chuẩn bị môi trường

- Git, Node.js và npm để phát triển frontend. Frontend đã được kiểm tra với Node.js 24 và npm 11.
- .NET SDK 8 và công cụ Entity Framework Core tương thích cho Auth.
- JDK và Maven tương thích với phiên bản Spring Boot trong `pom.xml`; cấu hình `JAVA_HOME` đúng JDK.
- PostgreSQL cho Trip, MySQL cho Booking; mỗi service cần tài khoản kết nối riêng.
- OpenSSL để tạo khóa RSA; cấu hình MoMo và SMTP khi triển khai thanh toán/email.
- Microsoft Edge nếu sử dụng script kiểm tra giao diện Playwright trong repository.

Kiểm tra công cụ:

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

Để chạy Station API, Trip cần `pom.xml`, lớp Application và cấu hình PostgreSQL. Các module nghiệp vụ khác vẫn cần được triển khai theo phạm vi của từng service.

## 7. Cấu hình local

| Thành phần | Tệp cấu hình | Giá trị cần khai báo |
| --- | --- | --- |
| Frontend | `frontend/.env.development` | `VITE_API_BASE_URL=http://localhost:8080` |
| Auth | `auth-service/src/AuthService.Api/appsettings.json` | Kết nối `FutaBus_AuthDb` (SQL Server), JWT Secret, SMTP Gmail, URL Frontend |
| Trip | `trip-service/src/main/resources/application.yaml` (local, không commit) | Cổng 8081 và kết nối PostgreSQL `trip_db`; cấu hình mẫu tại `application.example.yaml` |
| Booking | `booking-service/src/main/resources/application-local.yml` | Cổng 8082, kết nối `booking_db`, JWKS/issuer/audience, URL Trip, khóa nội bộ, MoMo và SMTP |
| Gateway | `gateway-1` | Cổng 8080, route service, JWKS/issuer/audience và origin CORS `http://localhost:5173` |

Tên các trường cấu hình backend phải khớp với lớp cấu hình của service. Không commit mật khẩu, token, secret MoMo, khóa RSA riêng hoặc tệp cấu hình local. Các tệp example chỉ chứa tên trường và giá trị không nhạy cảm.

Tạo cấu hình frontend từ thư mục gốc:

```powershell
Copy-Item frontend/.env.example frontend/.env.development
```

Tạo database `trip_db` trong PostgreSQL cho Trip Service:

```sql
CREATE DATABASE trip_db;
```

Tạo database `booking_db` trong MySQL cho Booking Service:

```sql
CREATE DATABASE IF NOT EXISTS booking_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
*(Lưu ý: Database `FutaBus_AuthDb` của Auth Service trên SQL Server sẽ tự động được tạo khi chạy lệnh `dotnet ef database update` hoặc chạy `AuthService.sln`).*

Cấp quyền theo database cho tài khoản service tương ứng và khai báo kết nối trong cấu hình local. SQL khởi tạo/seed dùng chung đặt tại `infra/mysql/`; thay đổi schema của từng service quản lý qua migration của service đó.

Tạo cặp khóa RSA từ thư mục gốc:

```powershell
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out auth-service/src/AuthService.Api/Keys/private.pem
openssl pkey -in auth-service/src/AuthService.Api/Keys/private.pem -pubout -out infra/keys/public.pem
```

Auth dùng khóa riêng để ký token và công bố khóa công khai tại `/.well-known/jwks.json`. Các service cấu hình JWKS URL `http://localhost:5001/.well-known/jwks.json`. Issuer, audience và tên claim role phải thống nhất toàn hệ thống.

## 8. Chạy toàn hệ thống

Thứ tự: **Database (SQL Server, PostgreSQL, MySQL) → Auth → Trip và Booking → Gateway → Frontend**. Chạy các service ở terminal riêng. Các đường dẫn trong các lệnh này tính từ thư mục gốc repository.

### Auth Service (.NET - Cổng 5001)

Có 2 cách chạy:
- **Cách 1 (Bằng Visual Studio - Khuyên dùng)**: Mở tệp `auth-service/AuthService.sln` bằng Visual Studio và nhấn nút **Play (http)** hoặc `Ctrl + F5`. Trình duyệt sẽ tự động mở Swagger UI tại `http://localhost:5001/swagger`.
- **Cách 2 (Bằng Terminal)**:
```powershell
dotnet ef database update --project auth-service/src/AuthService.Api
dotnet run --project auth-service/src/AuthService.Api --urls "http://localhost:5001"
```
*(Chi tiết cấu trúc, danh sách DTOs, endpoint Google Auth, câu lệnh CLI và 15 tài khoản seed sẵn cho 3 vai trò xem tại: `auth-service/readmeAuth.MD`).*

### Trip Service

```powershell
mvn -f trip-service/pom.xml spring-boot:run
```

Station API hiện chạy trên `trip-service` với PostgreSQL `trip_db`; schema được Hibernate cập nhật trong môi trường phát triển. Các API khác và xác thực nội bộ cần được triển khai trước khi chạy toàn bộ luồng Trip.

### Booking Service

```powershell
mvn -f booking-service/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Flyway chạy migrations của Booking. Khai báo thời gian giữ ghế, kết nối Trip, thông tin MoMo và SMTP. Endpoint giả lập kết quả thanh toán chỉ được bật ở profile phát triển.

### API Gateway (Cổng 8080)

Có thể chạy trực tiếp bằng script PowerShell tiện lợi:
```powershell
.\run-gateway.ps1
```
Hoặc qua lệnh Maven:
```powershell
mvn -f gateway/pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

Gateway tích hợp:
- Bộ lọc `InternalPathBlockFilter` tự động chặn mọi truy cập từ bên ngoài vào `/internal/**` với mã `403 Forbidden`.
- Bộ lọc CORS deduplication `DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials, RETAIN_FIRST` ngăn trùng lặp header CORS giữa Gateway và microservices.
- Định tuyến API hành khách/soát vé và `/api/trips/*/seats` tới Booking trước route tổng quát `/api/admin/**`, `/api/driver/**`, `/api/trips/**` tới Trip.
  *(Chi tiết cấu hình Gateway xem tại: `gateway-1`).*

### Frontend

```powershell
cd frontend
npm ci
npm run dev
```

Truy cập [http://localhost:5173](http://localhost:5173). API base URL lấy từ `.env.development`; khởi động lại Vite sau khi đổi biến môi trường. Trước khi kiểm tra luồng end-to-end, các feature phải được nối với API, Auth và dữ liệu database của service tương ứng.

Frontend hiện đọc dữ liệu trong `src/data/`; HTTP client và endpoint nằm ở `src/api/client.ts`. Khi tích hợp, chuyển việc tải dữ liệu sang module API và hook tương ứng, giữ kiểu dữ liệu dùng chung ổn định và kiểm tra quyền ở backend.

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
dotnet build auth-service/src/AuthService.Api
mvn -f trip-service/pom.xml verify
mvn -f booking-service/pom.xml verify
mvn -f gateway/pom.xml verify
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
| API trả 401/403 | Hạn token, chữ ký RSA/JWKS, issuer/audience, role và quyền sở hữu dữ liệu |
| Service không kết nối database | Kiểm tra đúng PostgreSQL cho Trip hoặc MySQL cho Booking, user/password, quyền, port và cấu hình local |
| Spring không nạp cấu hình local | Profile `local` đã bật, tệp `application-local.yml` đúng thư mục |
| Auth không đọc cấu hình | Environment `Development`, connection string và đường dẫn khóa RSA |
| Migration thất bại | Phiên bản migration, quyền database và dữ liệu hiện có; không xóa database để xử lý tùy tiện |
| MoMo không báo kết quả | Cấu hình provider, chữ ký, mã đơn/số tiền và khả năng truy cập IPN URL từ bên ngoài |
| Cổng đã được sử dụng | Xác định tiến trình dùng cổng và thống nhất cổng trong cấu hình các service |
