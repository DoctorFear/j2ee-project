import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { getRoleHomePath, selectIsAuthenticated, useAuthStore, type UserRole } from "@/features/auth";
import { SessionLoading } from "@/components/shared/SessionLoading";

export function ProtectedRoute({
  children,
  requiredRole,
}: {
  children: ReactNode;
  requiredRole?: UserRole | UserRole[];
}) {
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore(selectIsAuthenticated);
  const loading = useAuthStore((state) => state.loading);
  const location = useLocation();

  if (loading) return <SessionLoading />;
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;

  if (requiredRole) {
    const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];
    if (!user || !roles.includes(user.role as UserRole)) {
      return <Navigate to={getRoleHomePath(user?.role)} replace />;
    }
  }

  return children;
}
