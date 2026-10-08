// Thanh đầu trang khách hàng
import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  BusFront,
  ChevronDown,
  ContactRound,
  LogOut,
  ShieldCheck,
  Ticket,
} from "lucide-react";
import { demoContact } from "../data/customer";

// Hiển thị menu tài khoản và huy hiệu thanh toán
export default function CustomerHeader({
  booking = false,
}: {
  booking?: boolean;
}) {
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
      ) : (
        <div className="user-menu-container" ref={ref}>
          <button
            className="user-pill"
            aria-expanded={open}
            aria-controls="customer-menu"
            onClick={() => setOpen(!open)}
          >
            <div className="user-avatar">
              {demoContact.name.split(" ").at(-1)![0]}
            </div>
            <span>{demoContact.name}</span>
            <ChevronDown size={12} />
          </button>
          <div
            id="customer-menu"
            className={`user-dropdown ${open ? "active" : ""}`}
          >
            <Link to="/profile" onClick={() => setOpen(false)}>
              <ContactRound size={16} />
              Thông tin cá nhân
            </Link>
            <Link to="/my-tickets" onClick={() => setOpen(false)}>
              <Ticket size={16} />
              Danh sách vé
            </Link>
            <Link to="/login" className="logout" onClick={() => setOpen(false)}>
              <LogOut size={16} />
              Đăng xuất
            </Link>
          </div>
        </div>
      )}
    </header>
  );
}
