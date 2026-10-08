// Định tuyến các trang khách hàng
import { Link, Route, Routes } from "react-router-dom";
import SearchTripPage from "./features/customer/SearchTripPage";
import SeatBookingPage from "./features/customer/SeatBookingPage";
import MyTicketsPage from "./features/customer/MyTicketsPage";

// Chọn trang theo đường dẫn và xử lý trang chưa triển khai
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<SearchTripPage />} />
      <Route path="/trips/:code/book" element={<SeatBookingPage />} />
      <Route path="/my-tickets" element={<MyTicketsPage />} />
      <Route
        path="*"
        element={
          <main className="fallback">
            <h1>Trang chưa được triển khai</h1>
            <p>Đăng nhập và hồ sơ sẽ được kết nối với phần Auth của nhóm.</p>
            <Link to="/">Về tìm chuyến</Link>
          </main>
        }
      />
    </Routes>
  );
}
