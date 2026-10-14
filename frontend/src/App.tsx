// Định tuyến các trang khách hàng, phân hệ Quản trị và Tài xế FutaBus
import { Link, Route, Routes } from "react-router-dom";
import { UserProvider } from "./context/UserContext";
import { ProtectedRoute, CustomerRoute } from "./components/common/ProtectedRoute";

// Trang khách hàng (Member)
import SearchTripPage from "./features/customer/SearchTripPage";
import SeatBookingPage from "./features/customer/SeatBookingPage";
import MyTicketsPage from "./features/customer/MyTicketsPage";

// Trang Xác thực & Hồ sơ
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import ResetPasswordPage from "./pages/ResetPasswordPage";
import ProfilePage from "./pages/ProfilePage";

// Layout & Trang Quản trị (Admin)
import { AdminLayout } from "./layouts/admin/AdminLayout";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";

// Trang Tài xế / Shiper (Driver)
import DriverPage from "./pages/driver/DriverPage";

export default function App() {
  return (
    <UserProvider>
      <Routes>
        {/* ========================================================
            1. CÁC TRANG CÔNG KHAI & KHÁCH HÀNG (MEMBER)
           ======================================================== */}
        <Route
          path="/"
          element={
            <CustomerRoute>
              <SearchTripPage />
            </CustomerRoute>
          }
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        <Route
          path="/trips/:code/book"
          element={
            <CustomerRoute>
              <SeatBookingPage />
            </CustomerRoute>
          }
        />

        {/* Các trang Khách hàng yêu cầu đăng nhập */}
        <Route
          path="/my-tickets"
          element={
            <ProtectedRoute requiredRole="Member">
              <MyTicketsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute requiredRole="Member">
              <ProfilePage />
            </ProtectedRoute>
          }
        />

        {/* ========================================================
            2. PHÂN HỆ QUẢN TRỊ (ADMIN) CÓ SIDEBAR RIÊNG BIỆT
           ======================================================== */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Bảng điều khiển Quản trị">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/routes"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Quản lý Tuyến & Lộ trình trạm dừng">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/trips"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Lịch chạy chuyến & Phân công điều phối">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reports"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Báo cáo Doanh thu & Tỷ lệ lấp đầy">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/vehicles"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Quản lý Phương tiện & Xe khách">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/drivers"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout title="Quản lý Danh sách Tài xế">
                <AdminDashboardPage />
              </AdminLayout>
            </ProtectedRoute>
          }
        />

        {/* ========================================================
            3. PHÂN HỆ TÀI XẾ / SHIPER (DRIVER)
           ======================================================== */}
        <Route
          path="/driver"
          element={
            <ProtectedRoute requiredRole="Driver">
              <DriverPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/driver/trips"
          element={
            <ProtectedRoute requiredRole="Driver">
              <DriverPage />
            </ProtectedRoute>
          }
        />

        {/* ========================================================
            4. TRANG 404 / CHƯA TRIỂN KHAI
           ======================================================== */}
        <Route
          path="*"
          element={
            <main className="fallback" style={{ textAlign: "center", padding: "60px 20px" }}>
              <h1 style={{ fontSize: "24px", fontWeight: 800, color: "#0f172a", marginBottom: "12px" }}>
                Trang chưa được triển khai
              </h1>
              <p style={{ color: "#64748b", marginBottom: "20px" }}>
                Đường dẫn bạn yêu cầu hiện đang được phát triển bởi các thành viên trong nhóm.
              </p>
              <Link
                to="/"
                style={{
                  display: "inline-block",
                  padding: "10px 20px",
                  background: "var(--primary)",
                  color: "#ffffff",
                  borderRadius: "10px",
                  fontWeight: 700,
                  textDecoration: "none",
                }}
              >
                Về trang chủ tìm chuyến
              </Link>
            </main>
          }
        />
      </Routes>
    </UserProvider>
  );
}
