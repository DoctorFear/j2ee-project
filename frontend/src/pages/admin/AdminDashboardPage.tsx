// Trang chủ tạm thời của Quản trị viên (Admin Dashboard)
import React from "react";
import { useUser } from "../../context/UserContext";
import { MapPin, Bus, Users, CalendarCheck, Info } from "lucide-react";

export default function AdminDashboardPage() {
  const { user } = useUser();

  const statCards = [
    { title: "Tuyến liên tỉnh", value: "50", desc: "Đã sẵn sàng khai thác", icon: <MapPin size={22} color="#f95721" /> },
    { title: "Lịch chuyến hôm nay", value: "128", desc: "Đang mở bán", icon: <CalendarCheck size={22} color="#059669" /> },
    { title: "Phương tiện hoạt động", value: "45", desc: "Xe giường nằm & Limousine", icon: <Bus size={22} color="#3b82f6" /> },
    { title: "Tài xế sẵn sàng", value: "60", desc: "Đã phân công ca trực", icon: <Users size={22} color="#8b5cf6" /> },
  ];

  return (
    <div>
      {/* Banner chào mừng */}
      <div
        style={{
          background: "linear-gradient(135deg, #0f172a, #1e293b)",
          color: "#ffffff",
          padding: "28px 32px",
          borderRadius: "16px",
          marginBottom: "28px",
          boxShadow: "0 8px 24px -4px rgba(15, 23, 42, 0.15)",
        }}
      >
        <h1 style={{ fontSize: "22px", fontWeight: 800, marginBottom: "8px", color: "#f8fafc" }}>
          Xin chào, {user?.fullName || "Quản trị viên"}!
        </h1>
        <p style={{ color: "#94a3b8", fontSize: "14px", margin: 0 }}>
          Hệ thống điều phối xe khách đường dài FUTA Dispatcher. Sidebar quản trị bên trái đã được thiết lập sẵn sàng.
        </p>
      </div>

      {/* Thẻ thống kê tổng quan */}
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
          gap: "18px",
          marginBottom: "28px",
        }}
      >
        {statCards.map((card, idx) => (
          <div
            key={idx}
            style={{
              background: "#ffffff",
              padding: "20px",
              borderRadius: "14px",
              border: "1px solid #e2e8f0",
              boxShadow: "0 2px 10px rgba(0,0,0,0.03)",
              display: "flex",
              alignItems: "center",
              gap: "16px",
            }}
          >
            <div
              style={{
                width: "46px",
                height: "46px",
                borderRadius: "12px",
                background: "#f8fafc",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                border: "1px solid #e2e8f0",
              }}
            >
              {card.icon}
            </div>

            <div>
              <div style={{ fontSize: "12.5px", fontWeight: 600, color: "#64748b" }}>{card.title}</div>
              <div style={{ fontSize: "22px", fontWeight: 800, color: "#0f172a" }}>{card.value}</div>
              <div style={{ fontSize: "11px", color: "#94a3b8" }}>{card.desc}</div>
            </div>
          </div>
        ))}
      </div>

      {/* Hộp thông báo */}
      <div
        style={{
          background: "#ecfdf5",
          border: "1.5px solid #a7f3d0",
          borderRadius: "14px",
          padding: "20px 24px",
          color: "#065f46",
          display: "flex",
          alignItems: "flex-start",
          gap: "14px",
        }}
      >
        <Info size={22} style={{ flexShrink: 0, marginTop: "2px", color: "#059669" }} />
        <div>
          <h4 style={{ fontSize: "15px", fontWeight: 800, marginBottom: "4px" }}>
            Khung Sidebar Quản trị FUTA Dispatcher đã hoàn tất
          </h4>
          <p style={{ fontSize: "13.5px", lineHeight: 1.5, margin: 0 }}>
            Bạn đang đăng nhập với quyền <strong>Quản trị viên (Admin)</strong>. Bạn có thể sử dụng Sidebar bên trái để điều hướng giữa các chức năng quản lý tuyến xe, lịch chuyến, báo cáo doanh thu và nhân sự xe.
          </p>
        </div>
      </div>
    </div>
  );
}
