import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { getRoleHomePath, selectIsAuthenticated, useAuthStore } from "@/features/auth";

export function CustomerRoute({ children }: { children: ReactNode }) {
  const user = useAuthStore((state) => state.user);

  const isAuthenticated = useAuthStore(selectIsAuthenticated);

  if (isAuthenticated && (user?.role === "Admin" || user?.role === "Driver")) {
    return <Navigate to={getRoleHomePath(user.role)} replace />;
  }

  return children;
}
