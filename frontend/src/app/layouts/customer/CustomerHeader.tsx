// Thanh đầu trang khách hàng dùng phiên đăng nhập chung của FutaBus.
import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { BusFront, ChevronDown, ContactRound, LogOut, Search, ShieldCheck, Ticket, User } from "lucide-react";
import { useUser } from "@/features/auth";
import "./CustomerHeader.css";

// Hiển thị menu tài khoản hoặc nút đăng nhập/đăng ký
export default function CustomerHeader({ booking = false }: { booking?: boolean }) {
  const { user, isAuthenticated, logoutClient } = useUser();
  const navigate = useNavigate();
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

  const displayName = user?.fullName || user?.username || "Khách hàng";
  const initialLetter = displayName.split(" ").filter(Boolean).pop()?.charAt(0).toUpperCase() || "U";

  return (
    <header className="app-header">
      <Link to="/" className="brand-logo">
        <div className="brand-icon">
          <BusFront size={20} />
        </div>
        <div className="brand-title">
          <h2>FUTA Bus Lines</h2>
          <span>{booking ? "Đặt vé trực tuyến" : "Hệ thống vé đường dài"}</span>
        </div>
      </Link>

      {booking ? (
        <div className="header-badge">
          <ShieldCheck size={14} />
          <span>Thanh toán an toàn MoMo</span>
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
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <Link
            to="/login"
            style={{
              padding: "7px 16px",
              fontSize: "13px",
              fontWeight: 700,
              color: "var(--primary)",
              background: "var(--primary-light)",
              borderRadius: "var(--radius-full)",
              display: "flex",
              alignItems: "center",
              gap: "6px",
              textDecoration: "none",
              transition: "all 0.2s ease",
            }}
          >
            <User size={15} />
            <span>Đăng nhập</span>
          </Link>
          <Link
            to="/register"
            style={{
              padding: "7px 16px",
              fontSize: "13px",
              fontWeight: 700,
              color: "#ffffff",
              background: "linear-gradient(135deg, var(--primary), var(--primary-hover))",
              borderRadius: "var(--radius-full)",
              boxShadow: "0 4px 12px rgba(249, 87, 33, 0.25)",
              textDecoration: "none",
              transition: "all 0.2s ease",
            }}
          >
            Đăng ký
          </Link>
        </div>
      )}
    </header>
  );
}
