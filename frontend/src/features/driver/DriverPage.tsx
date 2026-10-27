// Trang dành cho Tài xế / Shiper (Driver) - Chuyển hướng vào và hiển thị khung thông báo
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { BusFront, Truck, LogOut, ArrowLeft, ShieldCheck } from "lucide-react";
import { useUser } from "@/features/auth";

export default function DriverPage() {
  const { user, logoutClient } = useUser();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await logoutClient();
    } finally {
      navigate("/login");
    }
  };

  return (
    <div
      style={{
        minHeight: "100vh",
        background: "#f8fafc",
        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* Header riêng của Tài xế */}
      <header
        style={{
          background: "#ffffff",
          borderBottom: "1px solid #e2e8f0",
          padding: "16px 36px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <div
            style={{
              width: "40px",
              height: "40px",
              background: "linear-gradient(135deg, #059669, #10b981)",
              color: "#ffffff",
              borderRadius: "10px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            <Truck size={22} />
          </div>
          <div>
            <h2 style={{ fontSize: "17px", fontWeight: 800, color: "#007646", margin: 0 }}>FUTA Driver & Check-in</h2>
            <span
              style={{
                fontSize: "11px",
                fontWeight: 700,
                color: "#f95721",
                textTransform: "uppercase",
              }}
            >
              Phân hệ Tài xế / Soát vé
            </span>
          </div>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <span
            style={{
              background: "#ecfdf5",
              color: "#059669",
              padding: "4px 12px",
              borderRadius: "9999px",
              fontSize: "12px",
              fontWeight: 700,
              display: "flex",
              alignItems: "center",
              gap: "4px",
            }}
          >
            <ShieldCheck size={14} />
            Driver Role
          </span>

          <button
            type="button"
            onClick={handleLogout}
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              padding: "7px 14px",
              border: "1px solid #fecaca",
              background: "#fef2f2",
              color: "#dc2626",
              borderRadius: "8px",
              fontSize: "13px",
              fontWeight: 700,
              cursor: "pointer",
            }}
          >
            <LogOut size={16} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </header>

      {/* Nội dung trang Tài xế */}
      <main
        style={{
          flex: 1,
          maxWidth: "760px",
          margin: "48px auto",
          padding: "0 20px",
          width: "100%",
        }}
      >
        <div
          style={{
            background: "#ffffff",
            borderRadius: "20px",
            border: "1px solid #e2e8f0",
            boxShadow: "0 10px 30px -4px rgba(15, 23, 42, 0.06)",
            padding: "36px 32px",
            textAlign: "center",
          }}
        >
          <div
            style={{
              width: "64px",
              height: "64px",
              borderRadius: "50%",
              background: "#ecfdf5",
              color: "#059669",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "0 auto 20px",
            }}
          >
            <BusFront size={32} />
          </div>

          <h1 style={{ fontSize: "22px", fontWeight: 800, color: "#0f172a", marginBottom: "8px" }}>
            Xin chào Bác tài, {user?.fullName || "Tài xế FUTA"}!
          </h1>
          <p style={{ color: "#64748b", fontSize: "14px", lineHeight: 1.6, marginBottom: "28px" }}>
            Bạn đã đăng nhập thành công vào phân hệ <strong>Tài xế / Shiper (Driver)</strong> với tài khoản{" "}
            <strong>@{user?.username}</strong>.
            <br />
            Hiện tại các chức năng Quản lý danh sách chuyến, Gọi điện đón khách và Soát vé QR đang được các thành viên
            trong nhóm hoàn thiện.
          </p>

          <div style={{ display: "flex", justifyContent: "center", gap: "12px" }}>
            <Link
              to="/"
              style={{
                display: "inline-flex",
                alignItems: "center",
                gap: "8px",
                padding: "10px 20px",
                background: "#f1f5f9",
                color: "#334155",
                borderRadius: "10px",
                fontWeight: 700,
                fontSize: "14px",
                textDecoration: "none",
              }}
            >
              <ArrowLeft size={16} />
              <span>Về trang chủ tìm chuyến</span>
            </Link>
          </div>
        </div>
      </main>
    </div>
  );
}
