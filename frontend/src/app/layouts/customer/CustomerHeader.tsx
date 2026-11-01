// Thanh đầu trang khách hàng dùng phiên đăng nhập chung của FutaBus.
import { useEffect, useRef, useState } from "react";
import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { ArrowLeft, BusFront, ChevronDown, ContactRound, LogOut, Search, ShieldCheck, Ticket, User } from "lucide-react";
import { useUser } from "@/features/auth";
import "./CustomerHeader.css";

// Hiển thị menu tài khoản hoặc nút đăng nhập/đăng ký
export default function CustomerHeader({ booking = false }: { booking?: boolean }) {
  const { user, isAuthenticated, logoutClient } = useUser();
  const navigate = useNavigate();
  const location = useLocation();
  const canGoBack = location.pathname !== "/";
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  // Đóng menu khi bấm ngoài hoặc nhấn Escape
  useEffect(() => {
    function close(event: PointerEvent) {
      if (!ref.current?.contains(event.target as Node)) setOpen(false);
    }
    function escape(event: KeyboardEvent) {
      if (event.key === "Escape") setOpen(false);
    }
    document.addEventListener("pointerdown", close);
    document.addEventListener("keydown", escape);
    return () => {
      document.removeEventListener("pointerdown", close);
      document.removeEventListener("keydown", escape);
    };
  }, []);

  const handleLogout = async () => {
    setOpen(false);
    try {
      await logoutClient();
    } finally {
      navigate("/login");
    }
  };

  // Quay về trang trước; nếu mở trực tiếp (không có lịch sử) thì về trang chủ
  const goBack = () => {
    if ((window.history.state?.idx ?? 0) > 0) navigate(-1);
    else navigate("/");
  };

  const displayName = user?.fullName || user?.username || "Khách hàng";
  const initialLetter = displayName.split(" ").filter(Boolean).pop()?.charAt(0).toUpperCase() || "U";

  return (
    <header className="app-header">
      {canGoBack && (
            <button type="button" className="header-back" onClick={goBack} aria-label="Quay lại trang trước">
              <ArrowLeft size={18} />
              <span>Quay lại</span>
            </button>
      )}
      <Link to="/" className="brand-logo">
        <div className="brand-icon">
          <BusFront size={20} />
        </div>
        <div className="brand-title">
          <h2>FUTA Bus Lines</h2>
          <span>{booking ? "Đặt vé trực tuyến" : "Hệ thống vé đường dài"}</span>
        </div>
      </Link>

      {!booking && (
        <nav className="header-nav" aria-label="Điều hướng chính">
          <NavLink to="/" end className={({ isActive }) => `header-nav-link ${isActive ? "active" : ""}`}>
            <Search size={15} />
              Tìm chuyến
          </NavLink>
          {isAuthenticated && user && (
            <NavLink to="/my-tickets" className={({ isActive }) => `header-nav-link ${isActive ? "active" : ""}`}>
              <Ticket size={15} />
              Vé của tôi
            </NavLink>
          )}
        </nav>
      )}

      {booking ? (
        <div className="header-badge">
          <ShieldCheck size={14} />
          <span>Thanh toán an toàn PayOS</span>
        </div>
      ) : isAuthenticated && user ? (
        <div className="user-menu-container" ref={ref}>
          <button
            className="user-pill"
            aria-expanded={open}
            aria-controls="customer-menu"
            onClick={() => setOpen(!open)}
          >
            <div className="user-avatar">{initialLetter}</div>
            <span>{displayName}</span>
            <ChevronDown size={12} />
          </button>

          <div id="customer-menu" className={`user-dropdown ${open ? "active" : ""}`}>
            <Link to="/" onClick={() => setOpen(false)}>
              <Search size={16} />
              Tìm chuyến xe
            </Link>

            <Link to="/my-tickets" onClick={() => setOpen(false)}>
              <Ticket size={16} />
              Danh sách vé đã đặt
            </Link>

            <Link to="/profile" onClick={() => setOpen(false)}>
              <ContactRound size={16} />
              Hồ sơ cá nhân
            </Link>

            <button type="button" className="logout" onClick={handleLogout}>
              <LogOut size={16} />
              <span>Đăng xuất</span>
            </button>
          </div>
        </div>
      ) : (
        <div className="header-auth">
          <Link to="/login" className="header-login">
            <User size={15} />
            <span>Đăng nhập</span>
          </Link>
          <Link to="/register" className="header-register">
            Đăng ký
          </Link>
        </div>
      )}
    </header>
  );
}
