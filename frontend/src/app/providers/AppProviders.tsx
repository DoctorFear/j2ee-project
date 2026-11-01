import type { ReactNode } from "react";
import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { env } from "@/lib/env";
import { AuthSession } from "./AuthSession";
import { ErrorBoundary } from "./ErrorBoundary";
import { ToastProvider } from "@/components/shared/toast/ToastProvider";

export function AppProviders({ children }: { children: ReactNode }) {
  const content = (
    <BrowserRouter>
      <AuthSession>{children}</AuthSession>
    </BrowserRouter>
  );

  return (
    <ErrorBoundary>
      <ToastProvider>
        {env.googleClientId ? (
            <GoogleOAuthProvider clientId={env.googleClientId}>{content}</GoogleOAuthProvider>
        ) : (
            content
        )}
      </ToastProvider>
    </ErrorBoundary>
  );
}
