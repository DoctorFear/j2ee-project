# Frontend FUTA

Ứng dụng React và TypeScript cho khách hàng, quản trị vận tải và tài xế. Frontend dùng Vite để phát triển và tạo bản build tĩnh. Xem [README toàn hệ thống](../README.md) để chạy backend.

## Chức năng chính

- **Admin:** dashboard và tám nhóm dữ liệu Transport; tạo, xem, xóa và sửa Station theo API hiện có. Có giao diện sáng/tối và menu mobile.
- **Tài khoản Admin:** xem họ tên, tên đăng nhập, email, số điện thoại và vai trò tại `/admin/account`; mở từ avatar hoặc menu “Thông tin tài khoản”.
- **Tài khoản:** đăng nhập, Google Sign-In, phân quyền tuyến đường, quản lý hồ sơ và phiên đăng nhập dùng chung.
- **Khách hàng:** tìm chuyến, chọn ghế, thanh toán minh họa và xem vé bằng dữ liệu demo. Các luồng này chưa tích hợp Booking Service.
- **Tài xế:** trang thông báo dành cho role Driver; chưa có nghiệp vụ điều hành riêng.

## Cài đặt

Cần Node.js 22.12+, 24 hoặc 26+ và npm. Có thể dùng Node.js 24 để chạy local.

Trong thư mục `frontend/`:

```bash
npm ci
cp .env.example .env.development
npm run dev
```

Với PowerShell, dùng `Copy-Item .env.example .env.development` thay cho `cp`. Chỉ sao chép khi chưa có cấu hình riêng. Mở http://127.0.0.1:5173.

## Sử dụng

Để đăng nhập và dùng Admin, chạy Auth Service, Transport Service và Gateway theo README toàn hệ thống. Đăng nhập bằng tài khoản có role phù hợp do Auth Service cấp.

| URL                                                      | Chức năng                                    |
| -------------------------------------------------------- | -------------------------------------------- |
| `/`                                                      | Tìm chuyến demo                              |
| `/trips/:code/book`                                      | Chọn ghế và thanh toán minh họa              |
| `/my-tickets`, `/profile`                                | Vé demo và hồ sơ Member                      |
| `/login`, `/register`                                    | Đăng nhập, đăng ký                           |
| `/forgot-password`, `/reset-password`                    | Khôi phục mật khẩu; xem giới hạn bên dưới    |
| `/admin`                                                 | Tổng quan vận hành                           |
| `/admin/account`                                         | Xem thông tin tài khoản Admin đang đăng nhập |
| `/admin/stations`, `/admin/routes`, `/admin/route-stops` | Trạm, tuyến và lộ trình                      |
| `/admin/vehicles`, `/admin/drivers`, `/admin/trips`      | Phương tiện, tài xế và chuyến                |
| `/admin/trip-drivers`, `/admin/trip-statistics`          | Phân công và thống kê                        |
| `/driver/trips`                                          | Khu vực Driver                               |

## Cấu hình

| Biến                    | Ý nghĩa                                                                                |
| ----------------------- | -------------------------------------------------------------------------------------- |
| `VITE_API_BASE_URL`     | URL Gateway chạy local, ví dụ `http://localhost:8080`.                                 |
| `VITE_GOOGLE_CLIENT_ID` | Client ID Google OAuth dùng khi chạy local. Bỏ trống để không khởi tạo Google Sign-In. |

Khởi động lại Vite khi sửa `.env.development`. Biến `VITE_*` được đưa vào trình duyệt, chỉ chứa cấu hình công khai.

## Cấu trúc mã nguồn

```text
src/
  app/
    routes.tsx         # Nơi duy nhất khai báo toàn bộ route
    guards/            # Kiểm tra đăng nhập và role trước khi mở màn hình
    layouts/           # Layout Admin, header Customer
    providers/         # Context và vòng đời ứng dụng
    pages/             # Các trang lỗi hệ thống
    stores/            # Trạng thái giao diện của layout
  api/                 # HTTP client, token storage và chuẩn hóa lỗi
  components/
    ui/                # Button, input, dialog, table và UI cơ bản
    shared/            # Bảng dữ liệu, thông báo và trạng thái hiển thị
  features/
    auth/              # Phiên, API và các trang xác thực
    admin/             # Nghiệp vụ tài khoản Admin, dashboard và Transport
    account/           # Hồ sơ và đổi mật khẩu
    customer/          # Search, booking, tickets, model và demo
    driver/            # Trang Driver
  lib/                 # Cấu hình, định dạng và hook tiện ích
  styles/              # CSS nền
  test/                # Thiết lập kiểm thử
```

Toàn bộ route, kể cả các trang Admin, được khai báo tại `src/app/routes.tsx`. Layout/provider thuộc `app/`; UI chung thuộc `components/`; feature giữ màn hình, dữ liệu và nghiệp vụ. ESLint kiểm tra ranh giới này.

Phần quản trị Transport được chia theo nghiệp vụ trong `src/features/admin/transport/`:

```text
transport/
  stations/            # Trạm và bến xe
    api.ts             # Gọi API trạm, bao gồm cập nhật
    module.ts          # Cột bảng, trường nhập và giá trị mặc định
    schema.ts          # Kiểm tra dữ liệu tạo và sửa trạm
    types.ts           # Kiểu dữ liệu trạm
    pages/             # Trang danh sách và chi tiết
  routes/              # Tuyến vận tải
  route-stops/         # Điểm dừng, kèm hook và bộ lọc theo tuyến
  vehicles/            # Phương tiện
  drivers/             # Tài xế
  trips/               # Chuyến xe và thông tin liên quan
  trip-drivers/        # Phân công tài xế
  trip-statistics/     # Thống kê chuyến
  shared/
    api/               # Cách gọi các endpoint CRUD có cùng cấu trúc
    hooks/             # Mỗi hook phụ trách một việc: tải, lưu, xóa, form hoặc danh sách
    queries/           # Khóa cache và cấu hình truy vấn
    components/        # Bảng, bộ lọc, trường nhập và dialog dùng lại
    pages/             # Khung danh sách và chi tiết nhận cấu hình từ nghiệp vụ
    model/             # Chọn, sắp xếp dữ liệu và đọc trạng thái từ URL
    validation/        # Quy tắc chung cho số, chuỗi và ngày giờ
    constants/         # Nhãn hiển thị
    format/            # Định dạng giá trị
    forms/             # Tạo cấu hình trường và nối schema với form
    types/             # Kiểu dữ liệu cho phần dùng chung
  navigation.ts        # Tiêu đề menu, không chứa schema hoặc màn hình
```

Các module nghiệp vụ có cùng cách tổ chức API, cấu hình, schema, kiểu dữ liệu và trang như `stations/`. Mỗi trang danh sách và chi tiết được tải riêng khi truy cập route. Khi sửa trường nhập của tài xế, thay đổi nằm trong `drivers/module.ts` và `drivers/schema.ts`.

Phần `shared/` nhận API và cấu hình từ module đang sử dụng. Nó không import ngược màn hình hoặc cấu hình của từng nghiệp vụ. Menu và header chỉ đọc `navigation.ts`; ESLint kiểm tra các ranh giới này.

Các phần ngoài Auth sử dụng `@/features/auth`; code nội bộ Auth import trực tiếp các file cùng module. HTTP dùng chung ở `src/api/`, còn API nghiệp vụ nằm trong feature tương ứng. Các bộ kiểm thử và tài liệu chi tiết được giữ local theo `.gitignore`.

## Kiểm tra

```bash
npm run check
```

Lệnh này chạy TypeScript, ESLint trên toàn bộ source, Prettier, unit/component test và kiểm tra build ngay trên máy. Dùng `npm run format` để định dạng mã.

E2E cần Postman CLI >=1.70 và Chromium:

```bash
npx playwright install chromium --only-shell
npm run test:e2e
npm run test:preview
```

`test:e2e` tự chạy Vite tại 5174 và Postman simulation tại 4900, kiểm tra Admin, Customer, ba role và HTTP/session. Fixture nằm trong `postman/mocks/transport-admin/`, dựa trên controller/DTO backend trong repository. Traffic thật của frontend được ghi ở `.test-output/simulation.ndjson`. Các test này không xác minh JWT hay database thật.

`test:preview` kiểm tra các trang công khai và tài nguyên từ `dist/` tại 5175; chạy sau build.

Để kiểm tra client với Java Transport và H2 riêng, dùng `npm run test:transport-live`. Cần Java/Maven Wrapper; runner tự khởi động và dừng backend tại 18081 với database trong bộ nhớ.

## Kiểm tra bản build tại local

```bash
npm run build
npm run preview
```

`build` tạo thư mục `dist/`; `preview` mở bản build tại http://127.0.0.1:4173. Nếu cần kiểm tra API trong bản build, đặt URL Gateway trước khi build. Ví dụ trong PowerShell:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run build
npm run preview
```

Khởi động lại preview sau khi build lại.

## Phạm vi tích hợp

- Form quên mật khẩu chỉ gửi email, trong khi backend yêu cầu thêm username. Chức năng này cần thống nhất lại contract.
- Tài khoản Google chưa có mật khẩu gửi `currentPassword` rỗng khi tạo mật khẩu, nhưng DTO backend hiện đánh dấu trường này bắt buộc. Cần sửa contract backend trước khi xác nhận luồng này.
- Guard frontend không thay thế kiểm tra quyền trên server. Transport/Gateway hiện chưa bảo vệ đầy đủ API Admin bằng JWT/role.
- Danh sách Transport chưa có phân trang server; Customer dùng demo và Driver là trang placeholder.
