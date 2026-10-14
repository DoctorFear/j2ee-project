// Route Guard bảo vệ chuẩn: Kiểm tra đăng nhập và tự động điều hướng đúng vai trò (không dùng trang 403 phức tạp)
import React from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useUser } from "../../context/UserContext";
import type { UserRole } from "../../types/user";

export const getRoleHomePath = (role?: string): string => {
  if (role === "Admin") return "/admin/routes";
  if (role === "Driver") return "/driver/trips";
  return "/";
};

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: UserRole | UserRole[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children, requiredRole }) => {
  const { user, isAuthenticated, loading } = useUser();
  const location = useLocation();

  if (loading) {
    return (
      <div
        style={{
          minHeight: "60vh",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          gap: "16px",
        }}
      >
        <div
          style={{
            width: "42px",
            height: "42px",
            border: "3px solid #f1f5f9",
            borderTopColor: "#f95721",
            borderRadius: "50%",
            animation: "spin 0.8s linear infinite",
          }}
        />
        <p style={{ color: "#64748b", fontWeight: 600, fontSize: "14px" }}>
          Đang xác thực thông tin tài khoản...
        </p>
      </div>
    );
  }

  // 1. Nếu chưa đăng nhập: chuyển hướng về /login
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // 2. Nếu đã đăng nhập nhưng sai vai trò: tự động chuyển về đúng khu vực của vai trò đó
  if (requiredRole) {
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    const hasPermission = user && roles.includes(user.role as UserRole);

    if (!hasPermission) {
      return <Navigate to={getRoleHomePath(user?.role)} replace />;
    }
  }

  return <>{children}</>;
};

// Route cho trang khách hàng (cho phép khách vãng lai hoặc Member, nhưng nếu Admin/Driver vào thì chuyển về phân hệ riêng)
export const CustomerRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, isAuthenticated } = useUser();

  if (isAuthenticated && user?.role === "Admin") {
    return <Navigate to="/admin/routes" replace />;
  }
  if (isAuthenticated && user?.role === "Driver") {
    return <Navigate to="/driver/trips" replace />;
  }

  return <>{children}</>;
};

