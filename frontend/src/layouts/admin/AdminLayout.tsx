// Layout Quản trị FUTA Dispatcher với Sidebar chuyên biệt theo chuẩn admin-quanlituyen.html
import React from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import {
  BusFront,
  MapPin,
  CalendarCheck,
  BarChart3,
  Bus,
  Users,
  LogOut,
  ShieldAlert,
} from "lucide-react";
import { useUser } from "../../context/UserContext";
import styles from "./AdminLayout.module.css";

interface AdminLayoutProps {
  children: React.ReactNode;
  title?: string;
}

export const AdminLayout: React.FC<AdminLayoutProps> = ({ children, title = "Bảng điều khiển Quản trị" }) => {
  const { user, logoutClient } = useUser();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logoutClient();
    navigate("/login");
  };

  const initial = user?.fullName ? user.fullName.split(" ").pop()?.charAt(0).toUpperCase() || "A" : "A";

  const navItems = [
    { to: "/admin/routes", label: "Tuyến & Lộ trình", icon: <MapPin size={18} /> },
    { to: "/admin/trips", label: "Lịch chuyến & Điều phối", icon: <CalendarCheck size={18} /> },
    { to: "/admin/reports", label: "Báo cáo & Doanh thu", icon: <BarChart3 size={18} /> },
    { to: "/admin/vehicles", label: "Phương tiện & Xe", icon: <Bus size={18} /> },
    { to: "/admin/drivers", label: "Tài xế & Nhân sự", icon: <Users size={18} /> },
  ];

  return (
    <div className={styles.adminContainer}>
      {/* 1. SIDEBAR RIÊNG BIỆT DÀNH CHO ADMIN */}
      <aside className={styles.sidebar}>
        <Link to="/admin/routes" className={styles.sidebarBrand}>
          <div className={styles.brandIcon}>
            <BusFront size={22} />
          </div>
          <div className={styles.brandTitle}>
            <h3>FUTA Dispatcher</h3>
            <span>Hệ thống Điều phối</span>
          </div>
        </Link>

        <ul className={styles.navList}>
          {navItems.map((item) => (
            <li key={item.to} className={styles.navItem}>
              <NavLink
                to={item.to}
                className={({ isActive }) => (isActive ? styles.navItemActive : undefined)}
              >
                {item.icon}
                <span>{item.label}</span>
              </NavLink>
            </li>
          ))}
        </ul>

        {/* Thông tin tài khoản Admin & Nút đăng xuất ở chân Sidebar */}
        <div className={styles.sidebarUser}>
          <div className={styles.userInfo}>
            <div className={styles.userAvatar}>{initial}</div>
            <div className={styles.userMeta}>
              <h4>{user?.fullName || "Quản trị viên"}</h4>
              <span>@{user?.username} (Admin)</span>
            </div>
          </div>

          <button
            type="button"
            className={styles.logoutBtn}
            onClick={handleLogout}
            title="Đăng xuất tài khoản quản trị"
          >
            <LogOut size={18} />
          </button>
        </div>
      </aside>

      {/* 2. MAIN CONTAINER */}
      <div className={styles.mainWrapper}>
        <header className={styles.topHeader}>
          <div className={styles.pageTitle}>{title}</div>

          <div className={styles.headerActions}>
            <span className={styles.roleBadge}>
              <ShieldAlert size={12} style={{ display: "inline", marginRight: "4px" }} />
              Admin Mode
            </span>
          </div>
        </header>

        <main className={styles.contentArea}>{children}</main>
      </div>
    </div>
  );
};
