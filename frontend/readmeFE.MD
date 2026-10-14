# Hướng Dẫn Kỹ Thuật & Cấu Trúc Frontend (FUTA Bus)

Tài liệu tóm tắt các bước triển khai, công dụng của thư viện và các tệp mã nguồn trong phân hệ **Frontend (đặc biệt là Auth, Google OAuth & Quản lý người dùng)**.

---

## 1. Thư viện sử dụng và mục đích

| Tên thư viện | Phiên bản | Công dụng |
| :--- | :--- | :--- |
| **react & react-dom** | `^19.0.0` | Thư viện nền tảng xây dựng giao diện người dùng theo Component. |
| **react-router-dom** | `^7.0.0` | Điều hướng trang (Routing) SPA, hỗ trợ `Routes`, `Route`, `useNavigate`, `useLocation`. |
| **@react-oauth/google** | `^0.12.1` | Tích hợp chuẩn Google OAuth 2.0 Sign-In (`GoogleOAuthProvider`, component `<GoogleLogin>`). |
| **lucide-react** | `^0.468.0` | Bộ icon hiện đại (xe khách `BusFront`, vé `Ticket`, ổ khóa `Lock`, mắt `Eye/EyeOff`, khiên `ShieldCheck`, user `User`). |
| **qrcode.react** | `^4.2.0` | Tạo mã QR cho vé xe điện tử để tài xế quét khi lên xe. |
| **typescript** | `~5.7.3` | Kiểm soát kiểu dữ liệu tĩnh nghiêm ngặt, tự động bắt lỗi lúc code và lúc build. |
| **vite** | `^6.0.5` | Công cụ đóng gói (Bundler) và máy chủ phát triển cực nhanh (HMR). |

---

## 2. Công dụng các tệp trong thư mục `src/`

```text
frontend/src/
├── types/
│   └── user.ts                    # Khai báo DTOs: Login, Register, GoogleLoginRequestDto, Profile, UserRole (3 roles), ApiResponse<T>
├── services/
│   ├── apiClient.ts               # Core HTTP client: Đính kèm token Bearer, bóc tách lỗi, tự động Silent Refresh ngầm khi gặp lỗi 401
│   ├── authService.ts             # Gọi API xác thực: /api/auth (login, google-login, register, logout, refresh-token, forgot/reset-password)
│   └── userService.ts             # Gọi API tài khoản: /api/user (getProfile, updateProfile, changePassword)
├── context/
│   └── UserContext.tsx            # Global State: Quản lý user, token, isAuthenticated, cờ hasPassword; tự nạp thông tin khi reload trang
├── hooks/
│   ├── useToastNotifications.ts   # Quản lý hàng đợi thông báo Toast (success, error, info)
│   └── useAuthAction.ts           # Điều phối nghiệp vụ Auth (submit form, báo lỗi qua toast, điều hướng theo role)
├── components/common/
│   ├── ProtectedRoute.tsx         # Route Guard thông minh: Kiểm tra đăng nhập và tự động điều hướng đúng phân hệ theo vai trò
│   └── ToastNotification.tsx      # Giao diện hiển thị popup Toast thông báo nổi màu chuẩn FUTA
├── pages/
│   ├── LoginPage.tsx / .module.css       # Màn hình Đăng nhập (Form đăng nhập hệ thống + Nút đăng nhập Google OAuth 2.0 chính thức)
│   ├── RegisterPage.tsx                  # Màn hình Đăng ký thành viên (validate SĐT 10 số VN, mật khẩu khớp)
│   ├── ForgotPasswordPage.tsx            # Màn hình Quên mật khẩu (nhập email nhận liên kết khôi phục)
│   ├── ResetPasswordPage.tsx             # Màn hình Đặt lại mật khẩu mới (nhận token từ link email)
│   └── ProfilePage.tsx / .module.css     # Màn hình Hồ sơ cá nhân (Tab 1: Sửa thông tin cá nhân; Tab 2: Tạo/Đổi mật khẩu theo cờ hasPassword)
├── layouts/
│   ├── CustomerHeader.tsx         # Header khách hàng: Tự đổi giữa nút Đăng nhập/Đăng ký và Avatar/Tên người dùng
│   ├── CustomerHeader.css         # CSS biệt lập cho CustomerHeader, ngăn chặn xung đột style toàn cục
│   ├── AdminLayout.tsx            # Bố cục trang Quản trị viên (Sidebar menu, quản lý tuyến/chuyến/báo cáo)
│   └── DriverLayout.tsx           # Bố cục trang Tài xế (Danh sách chuyến chạy, quét vé, nhật ký)
├── main.tsx                       # Bọc toàn bộ ứng dụng bằng GoogleOAuthProvider với clientId từ biến môi trường
├── App.tsx                        # Khai báo cây Route và bọc toàn bộ bằng UserProvider
└── .env                           # Khai báo URL Gateway và Google Client ID
```

---

## 3. Các bước triển khai kỹ thuật đã thực hiện

1. **Chuẩn hóa hợp đồng dữ liệu (`types/user.ts`)**:
   - Định nghĩa chính xác DTOs khớp với Backend .NET (`AuthService.Api`).
   - Phân quyền gồm **3 vai trò**: `Member` (Khách hàng), `Admin` (Quản trị viên) và `Driver` (Tài xế).
   - Bổ sung `GoogleLoginRequestDto` (`{ idToken: string }`) và thuộc tính `hasPassword?: boolean` để nhận biết tài khoản Google chưa có mật khẩu.

2. **Tích hợp Đăng nhập Google OAuth 2.0 (`LoginPage.tsx`)**:
   - Cung cấp component `<GoogleLogin>` chính thức từ thư viện `@react-oauth/google`.
   - Sau khi người dùng đồng ý cấp quyền trên Google pop-up, Frontend lấy `credential` (Google ID Token) và gửi tới `POST /api/auth/google-login` qua Gateway.
   - Tự động nhận diện vai trò và điều hướng chính xác: `Admin` $\rightarrow$ `/admin/routes`, `Driver` $\rightarrow$ `/driver/trips`, `Member` $\rightarrow$ `/`.

3. **Giao diện Hồ sơ & Mật khẩu linh hoạt (`ProfilePage.tsx`)**:
   - Nhận cờ `hasPassword` từ `UserContext` và API hồ sơ.
   - Nếu `hasPassword === false`: Tab đổi thành **"Tạo mật khẩu"**, form chỉ hiển thị **2 ô nhập** (Mật khẩu mới & Xác nhận mật khẩu mới). Khi gửi thành công, tự động gọi `refreshUserProfile()` để chuyển sang trạng thái đã có mật khẩu.
   - Nếu `hasPassword === true`: Tab hiển thị là **"Đổi mật khẩu"**, gồm đủ **3 ô nhập** (Mật khẩu hiện tại, Mật khẩu mới & Xác nhận mật khẩu).

4. **Bảo vệ Route thông minh (`ProtectedRoute.tsx`)**:
   - Kiểm tra trạng thái đăng nhập. Nếu chưa đăng nhập, tự động chuyển về `/login` và lưu đường dẫn hiện tại vào `state.from`.
   - Kiểm tra vai trò: Nếu người dùng không đủ quyền truy cập vào route yêu cầu, hệ thống tự động điều hướng người dùng về đúng khu vực của họ (`/admin/routes` cho Admin, `/driver/trips` cho Driver, `/` cho Member) thay vì đưa về trang 403 trung gian gây gián đoạn luồng sử dụng.

5. **Cô lập giao diện Header (`CustomerHeader.css`)**:
   - Tách toàn bộ CSS của header khách hàng thành file `CustomerHeader.css` độc lập.
   - Đảm bảo thanh điều hướng khách hàng không bị ảnh hưởng hay vỡ layout khi tích hợp cùng các trang quản trị hoặc trang cá nhân `/profile`.

6. **Xây dựng HTTP Client thông minh (`apiClient.ts`)**:
   - Tự động gắn header `Authorization: Bearer <token>`.
   - Bắt lỗi `401 Unauthorized` để tự động làm mới access token ngầm bằng Refresh Token (dùng Singleton Promise chống race-condition).

---

## 4. Cấu hình biến môi trường (`frontend/.env`)

Khai báo tệp `.env` tại thư mục `frontend/`:

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_GOOGLE_CLIENT_ID=1069494819691-vkr5q46np5ebdn76q3nl68s7q7lm7cij.apps.googleusercontent.com
```

---

## 5. Lệnh thao tác với Frontend

```powershell
# Di chuyển vào thư mục frontend
cd frontend

# Cài đặt thư viện phụ thuộc
npm install

# Khởi chạy môi trường phát triển (Port 5173)
npm run dev

# Kiểm tra lỗi TypeScript & đóng gói build sản phẩm
npm run build
```
