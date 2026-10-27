import { useEffect, type ReactNode } from "react";
import { useAuthStore } from "@/features/auth";
import { AUTH_EXPIRED_EVENT, subscribeToSessionRemoval } from "@/api/session";
import { SessionErrorPage } from "../pages/SessionErrorPage";

export function AuthSession({ children }: { children: ReactNode }) {
  const error = useAuthStore((state) => state.bootstrapError);
  const loading = useAuthStore((state) => state.loading);

  useEffect(() => {
    const { initializeAuth, clearSession } = useAuthStore.getState();

    window.addEventListener(AUTH_EXPIRED_EVENT, clearSession);
    const unsubscribeStorage = subscribeToSessionRemoval(clearSession);
    void initializeAuth();

    return () => {
      window.removeEventListener(AUTH_EXPIRED_EVENT, clearSession);
      unsubscribeStorage();
    };
  }, []);

  if (error)
    return (
      <SessionErrorPage error={error} loading={loading} onRetry={() => void useAuthStore.getState().initializeAuth()} />
    );

  return children;
}
