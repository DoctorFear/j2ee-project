# Frontend FUTA

Ứng dụng React và TypeScript phục vụ khách hàng, quản trị/điều phối và tài xế. Kiến trúc, phân công và quy trình chạy toàn hệ thống nằm trong [README chung](../README.md).

## Cài đặt và chạy

Trong thư mục `frontend/`:

```powershell
npm ci
Copy-Item .env.example .env.development
npm run dev
```

Truy cập http://localhost:5173. Các biến môi trường trong `.env` / `.env.development`:
- `VITE_API_BASE_URL=http://localhost:8080` (trỏ tới Spring Cloud Gateway)
- `VITE_GOOGLE_CLIENT_ID=1069494819691-vkr5q46np5ebdn76q3nl68s7q7lm7cij.apps.googleusercontent.com` (Google OAuth 2.0 Client ID)

Khởi động lại máy chủ phát triển Vite sau khi thay đổi biến môi trường. Chi tiết kiến trúc Auth, Google Sign-In và các trang xem tại: [readmeFE.MD](readmeFE.MD).

## Tổ chức mã nguồn

- `src/features/`: trang và component theo nhóm người dùng; kiểu cục bộ ở `features/<feature>/types/`.
- `src/types/`: kiểu dùng chung, mỗi thực thể/nghiệp vụ có tệp riêng như `trip.ts`, `ticket.ts`, `station.ts`, `seat.ts`, `vehicle.ts`, `pricing.ts`, `route-stop.ts`, `contact.ts`.
- `src/components/` và `src/layouts/`: thành phần và bố cục dùng chung.
- `src/hooks/`: logic trạng thái; `src/api/`: giao tiếp server; `src/utils/`: định dạng và tính toán; `src/data/`: dữ liệu và danh mục.

Import type trực tiếp từ tệp tương ứng. Trước khi tích hợp các service, cấu hình Auth, API và quyền theo README chung. Font sử dụng Plus Jakarta Sans; icon dùng Lucide.

## Kiểm tra

```powershell
npm run build
npm test
npm run preview
```

Sau khi khởi động Vite, chạy từ thư mục gốc repository:

```powershell
node frontend/scripts/check-ui.mjs
```

Script dùng Edge headless, kiểm tra luồng thao tác và bố cục desktop/mobile. Cập nhật đường dẫn trang tham chiếu trong script theo máy; ảnh kiểm tra lưu tại `screenshots/`.
