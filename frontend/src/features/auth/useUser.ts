import { selectIsAuthenticated, useAuthStore } from "@/features/auth/auth-store";

// Compatibility hook for customer/driver pages sharing the Zustand auth session.
export function useUser() {
  const state = useAuthStore();
  return { ...state, isAuthenticated: selectIsAuthenticated(state) };
}
