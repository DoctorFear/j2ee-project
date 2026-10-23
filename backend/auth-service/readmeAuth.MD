# Hướng Dẫn Kỹ Thuật & Cấu Trúc Backend (Auth Service - FutaBus)

Tài liệu này ghi lại ngắn gọn quy trình xây dựng, ý nghĩa các thư viện, các câu lệnh CLI cần nhớ và bố cục mã nguồn của dịch vụ **AuthService.Api** (.NET / SQL Server).

---

## 1. Công Dụng Của Các Thư Viện Đã Cài (NuGet Packages)

| Thư viện | Mục đích sử dụng |
| :--- | :--- |
| **`Microsoft.EntityFrameworkCore.SqlServer`** | Driver ORM chính chủ của Microsoft kết nối Entity Framework Core với **SQL Server** (`FutaBus_AuthDb`). |
| **`Microsoft.AspNetCore.Authentication.JwtBearer`** | Middleware xác thực phiên người dùng bằng **JWT Access Token** (15 phút). |
| **`Microsoft.EntityFrameworkCore.Design`** | Công cụ hỗ trợ chạy các lệnh CLI của Entity Framework (như tạo migration, update database). |
| **`BCrypt.Net-Next`** | Thư viện băm mật khẩu một chiều (**BCrypt**) kèm salt tự sinh an toàn, chống lộ mật khẩu trong DB. |
| **`Swashbuckle.AspNetCore`** | Tự động sinh tài liệu API và giao diện **Swagger UI** để test trực tiếp các endpoint kèm nút nhập Token. |

---

## 2. Các Lệnh CLI Quan Trọng (Cheat Sheet)

> **Lưu ý**: Mở Terminal tại thư mục `backend/auth-service` hoặc `backend/auth-service` trước khi gõ lệnh.

### a. Khôi phục thư viện & Build dự án
```powershell
dotnet restore
dotnet build
```

### b. Tạo Migration mới (khi sửa đổi hoặc thêm Entity)
```powershell
dotnet ef migrations add <TenMigration> -o Data/Migrations --project src/AuthService.Api
# Ví dụ: dotnet ef migrations add AddNewColumn -o Data/Migrations --project src/AuthService.Api
```

### c. Cập nhật bảng dữ liệu vào SQL Server
```powershell
dotnet ef database update --project src/AuthService.Api
```
*(Lệnh này tự động tạo database `FutaBus_AuthDb` và bảng `users` trong SQL Server nếu chưa có).*

### d. Xóa Migration gần nhất (nếu vừa tạo sai và chưa update DB)
```powershell
dotnet ef migrations remove --project src/AuthService.Api
```

### e. Chạy ứng dụng Auth Service
```powershell
dotnet run --project src/AuthService.Api --urls "http://localhost:5001"
```
* Sau khi chạy, truy cập Swagger để test API tại: `http://localhost:5001/swagger`

---

## 3. Bố Cục Thư Mục (Clean Architecture)

```text
auth-service/
├── AuthService.sln           # File Solution để mở trực tiếp trong Visual Studio
├── readmeAuth.MD             # File tài liệu hướng dẫn kỹ thuật này
└── src/
    └── AuthService.Api/
        ├── Controllers/              # Tầng tiếp nhận HTTP Request từ Frontend qua Gateway
        │   ├── AuthController.cs     # API: /api/auth (register, login, google-login, refresh-token, forgot/reset-password, logout)
        │   └── UserController.cs     # API: /api/user (profile, change-password)
        │
        ├── Data/                     # Tầng kết nối Cơ sở dữ liệu
        │   ├── AuthDbContext.cs      # Khai báo DbSet<User>, cấu hình Index duy nhất (Email, Username, GoogleId)
        │   ├── Migrations/           # Chứa các file Migration sinh tự động từ EF Core (AddGoogleAuth...)
        │   └── Seed/
        │       └── DatabaseSeeder.cs # Tự động seed 15 tài khoản mặc định (5 Admin, 5 Member, 5 Driver)
        │
        ├── Dtos/                     # Đối tượng truyền nhận dữ liệu (Data Transfer Objects)
        │   ├── Common/
        │   │   └── ApiResponse.cs    # Chuẩn định dạng phản hồi JSON chung { success, message, data }
        │   ├── Auth/                 # DTO cho đăng ký, đăng nhập, google-login (GoogleLoginRequestDto), refresh token
        │   └── User/                 # DTO cho xem/sửa hồ sơ cá nhân và đổi/tạo mật khẩu (UserProfileResponseDto có HasPassword)
        │
        ├── Entities/                 # Thực thể đại diện bảng trong CSDL
        │   ├── User.cs               # Bảng 'users' (Id, Username, Email, PasswordHash, GoogleId, HasPassword, Role...)
        │   └── UserRole.cs           # Enum phân quyền 3 vai trò: Member (khách hàng), Admin (quản trị), Driver (tài xế)
        │
        ├── Repositories/             # Tầng tương tác dữ liệu (DAL)
        │   ├── IUserRepository.cs    # Interface định nghĩa truy vấn User (GetByEmail, GetByGoogleId, ExistsByEmail...)
        │   └── UserRepository.cs     # Thực thi truy vấn qua EF Core và sinh username duy nhất GenerateUniqueUsernameAsync
        │
        ├── Services/                 # Tầng xử lý Logic nghiệp vụ chính (BLL)
        │   ├── IPasswordHasher.cs    # Interface mã hóa mật khẩu
        │   ├── BcryptPasswordHasher.cs # Băm & so khớp mật khẩu bằng BCrypt
        │   ├── ITokenService.cs      # Interface sinh JWT
        │   ├── TokenService.cs       # Cấp Access Token (15 phút) & Refresh Token ngẫu nhiên (3 ngày)
        │   ├── IEmailService.cs      # Interface gửi mail
        │   ├── EmailService.cs       # Gửi mail SMTP (Gmail) chứa liên kết đặt lại mật khẩu
        │   ├── IAuthService.cs       # Interface xác thực (Login, Register, GoogleLogin, RefreshToken, ForgotPassword)
        │   ├── AuthenticationService.cs # Xử lý logic Đăng nhập Google, Account Linking, sinh Username, xác thực Token
        │   ├── IUserService.cs       # Interface hồ sơ
        │   └── UserService.cs        # Xử lý Cập nhật hồ sơ (kiểm tra email duy nhất) & Tạo/Đổi mật khẩu theo HasPassword
        │
        ├── Properties/
        │   └── launchSettings.json   # Cấu hình Visual Studio bấm Run tự mở Swagger tại cổng 5001
        ├── appsettings.json          # Cấu hình kết nối SQL Server, JWT Secret Key, SMTP Gmail, URL Frontend
        └── Program.cs                # Điểm khởi động ứng dụng: Đăng ký DI, cấu hình CORS, JWT, Swagger, DB Seeder
```

---

## 4. Cơ Chế Google OAuth 2.0 & Bảo Mật Dữ Liệu

### a. Luồng xác thực Google Sign-In (`POST /api/auth/google-login`)
1. Frontend nhận Google ID Token từ thư viện `@react-oauth/google` và gửi lên `POST /api/auth/google-login`.
2. Backend sử dụng `HttpClient` gọi endpoint Google `https://oauth2.googleapis.com/tokeninfo?id_token={token}` để xác minh token thật, lấy `sub` (GoogleId), `email`, `name`, `email_verified`.
3. **Phân nhánh xử lý:**
   - **Đã tồn tại GoogleId**: Đăng nhập ngay, cấp Access Token & Refresh Token.
   - **Chưa có GoogleId nhưng Email đã tồn tại**:
     - Nếu `Role == Member`: Tự động liên kết tài khoản (Account Linking), gắn `GoogleId` vào user hiện tại.
     - Nếu `Role != Member` (`Admin` hoặc `Driver`): Từ chối ngay lập tức để bảo vệ tài khoản quản trị/vận hành, yêu cầu đăng nhập bằng tài khoản mật khẩu hệ thống.
   - **Email chưa từng tồn tại**: Tự động tạo tài khoản mới với vai trò `Member`, gọi `GenerateUniqueUsernameAsync` sinh tên đăng nhập sạch không bao giờ bị trùng, `PasswordHash = null`, `HasPassword = false`.

### b. Đảm bảo tính duy nhất của Email & Username
- **Database Index**:
  - `IX_users_Email`: Unique Index trên cột `Email` trong `users`.
  - `IX_users_Username`: Unique Index trên cột `Username` trong `users`.
  - `IX_users_GoogleId`: Unique Index có điều kiện `[GoogleId] IS NOT NULL`.
- **Validation tầng ứng dụng**:
  - Khi Đăng ký mới (`RegisterAsync`): Kiểm tra `ExistsByEmailAsync(email)` trước khi thêm vào DB.
  - Khi Cập nhật hồ sơ (`UpdateProfileAsync`): Kiểm tra email có thuộc về tài khoản khác hay không trước khi cập nhật.
  - Thuật toán `GenerateUniqueUsernameAsync`: Lọc bỏ ký tự đặc biệt, kiểm tra trong DB và tự động thêm số ngẫu nhiên / timestamp đảm bảo không xảy ra đụng độ (collision).

### c. Xử lý Tạo / Đổi Mật Khẩu linh hoạt (`ChangePasswordAsync`)
- Khi người dùng đăng ký qua Google, `HasPassword = false`.
- API `/api/user/change-password` tự động kiểm tra:
  - Nếu `user.HasPassword == false`: Cho phép người dùng tạo mật khẩu mới trực tiếp (bỏ qua kiểm tra mật khẩu hiện tại), sau đó cập nhật `HasPassword = true`.
  - Nếu `user.HasPassword == true`: Bắt buộc nhập đúng mật khẩu hiện tại (kiểm tra qua BCrypt) trước khi đổi sang mật khẩu mới.

---

## 5. Danh Sách 15 Tài Khoản Được Seed Tự Động (Mỗi Vai Trò 5 Tài Khoản)

Khi ứng dụng khởi chạy và kết nối Database thành công, seeder sẽ tự động tạo đủ 15 tài khoản cho 3 vai trò:

### a. 5 Tài khoản Quản trị viên (Admin) - Mật khẩu: `Admin@123`
| Username | Email | Họ và tên | SĐT | Vai trò |
| :--- | :--- | :--- | :--- | :--- |
| `admin` | `admin@futabus.vn` | Quản trị viên FutaBus | 0901234501 | Admin |
| `admin2` | `admin2@futabus.vn` | Quản trị viên Điều phối 2 | 0901234502 | Admin |
| `admin3` | `admin3@futabus.vn` | Quản trị viên Giám sát 3 | 0901234503 | Admin |
| `admin4` | `admin4@futabus.vn` | Quản trị viên Vận hành 4 | 0901234504 | Admin |
| `admin5` | `admin5@futabus.vn` | Quản trị viên Báo cáo 5 | 0901234505 | Admin |

### b. 5 Tài khoản Khách hàng (Member) - Mật khẩu: `Member@123`
| Username | Email | Họ và tên | SĐT | Vai trò |
| :--- | :--- | :--- | :--- | :--- |
| `member1` | `member1@futabus.vn` | Khách hàng Nguyễn Văn An | 0902345601 | Member |
| `member2` | `member2@futabus.vn` | Khách hàng Trần Thị Bình | 0902345602 | Member |
| `member3` | `member3@futabus.vn` | Khách hàng Lê Hoàng Cường | 0902345603 | Member |
| `member4` | `member4@futabus.vn` | Khách hàng Phạm Thu Duyên | 0902345604 | Member |
| `member5` | `member5@futabus.vn` | Khách hàng Vũ Đức Em | 0902345605 | Member |

### c. 5 Tài khoản Tài xế / Shiper (Driver) - Mật khẩu: `Driver@123`
| Username | Email | Họ và tên | SĐT | Vai trò |
| :--- | :--- | :--- | :--- | :--- |
| `driver1` | `driver1@futabus.vn` | Bác tài Trần Văn Hùng | 0983456701 | Driver |
| `driver2` | `driver2@futabus.vn` | Bác tài Nguyễn Thành Nam | 0983456702 | Driver |
| `driver3` | `driver3@futabus.vn` | Bác tài Lê Minh Tuấn | 0983456703 | Driver |
| `driver4` | `driver4@futabus.vn` | Bác tài Phạm Quốc Việt | 0983456704 | Driver |
| `driver5` | `driver5@futabus.vn` | Bác tài Đỗ Hải Đăng | 0983456705 | Driver |
