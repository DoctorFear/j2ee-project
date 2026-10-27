import type { ReactNode } from "react";
import { BrowserRouter } from "react-router-dom";
import { GoogleOAuthProvider } from "@react-oauth/google";
import { env } from "@/lib/env";
import { AuthSession } from "./AuthSession";
import { ErrorBoundary } from "./ErrorBoundary";

export function AppProviders({ children }: { children: ReactNode }) {
  const content = (
    <BrowserRouter>
      <AuthSession>{children}</AuthSession>
    </BrowserRouter>
  );

  return (
    <ErrorBoundary>
      {env.googleClientId ? (
        <GoogleOAuthProvider clientId={env.googleClientId}>{content}</GoogleOAuthProvider>
      ) : (
        content
      )}
    </ErrorBoundary>
  );
}
