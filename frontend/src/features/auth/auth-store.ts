import { create } from "zustand";
import { clearTokens, getAccessToken, setTokens } from "@/api/session";
import { sessionApi } from "./session-api";
import { ApiError } from "@/api/error";

import type { LoginResponseDto, UserInfo } from "@/features/auth/types";

interface AuthState {
  user: UserInfo | null;
  token: string | null;
  loading: boolean;
  bootstrapError: string | null;
  initializeAuth: () => Promise<void>;
  loginSuccess: (loginData: LoginResponseDto, rememberMe?: boolean) => void;
  logoutClient: () => Promise<void>;
  clearSession: () => void;
  updateUser: (updated: Partial<UserInfo>) => void;
  refreshUserProfile: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()((set, get) => {
  let initialization: Promise<void> | null = null;
  let sessionVersion = 0;

  return {
    user: null,
    token: getAccessToken(),
    loading: true,
    bootstrapError: null,

    initializeAuth: () => {
      // Reuse the pending request when React StrictMode runs the effect twice.
      if (initialization) return initialization;
      if (!get().loading && !get().bootstrapError) return Promise.resolve();

      const token = getAccessToken();
      if (!token) {
        set({ user: null, token: null, loading: false, bootstrapError: null });
        return Promise.resolve();
      }

      set({ token, loading: true, bootstrapError: null });
      initialization = get()
        .refreshUserProfile()
        .finally(() => {
          set({ loading: false });
          initialization = null;
        });
      return initialization;
    },

    refreshUserProfile: async () => {
      if (!getAccessToken()) {
        get().clearSession();
        return;
      }

      const version = sessionVersion;
      try {
        const profile = await sessionApi.getProfile();
        // A profile response from an old session must not restore that session.
        if (version !== sessionVersion) return;
        set({
          token: getAccessToken(),
          bootstrapError: null,
          user: {
            id: profile.id,
            username: profile.username,
            email: profile.email,
            fullName: profile.fullName,
            phoneNumber: profile.phoneNumber,
            role: profile.role,
            hasPassword: profile.hasPassword ?? true,
          },
        });
      } catch (error) {
        if (version !== sessionVersion) return;
        if (error instanceof ApiError && [401, 403].includes(error.status ?? 0)) {
          get().clearSession();
        } else {
          set({
            bootstrapError: error instanceof Error ? error.message : "Không thể tải thông tin tài khoản.",
          });
        }
      }
    },

    loginSuccess: (loginData, rememberMe = true) => {
      sessionVersion += 1;
      setTokens(loginData.accessToken, loginData.refreshToken, rememberMe);
      set({
        token: loginData.accessToken,
        loading: false,
        bootstrapError: null,
        user: {
          id: loginData.userId,
          username: loginData.username,
          email: loginData.email,
          fullName: loginData.fullName,
          role: loginData.role,
          hasPassword: loginData.hasPassword ?? true,
        },
      });
    },

    clearSession: () => {
      sessionVersion += 1;
      clearTokens();
      set({ user: null, token: null, loading: false, bootstrapError: null });
    },
    logoutClient: async () => {
      const logout = sessionApi.logout();
      get().clearSession();
      try {
        await logout;
      } catch {
        // Local logout is complete even if the server cannot revoke the token.
        if (import.meta.env.DEV) console.warn("Không thể thu hồi phiên trên máy chủ.");
      }
    },

    updateUser: (updated) => {
      set((state) => ({ user: state.user ? { ...state.user, ...updated } : null }));
    },
  };
});

export const selectIsAuthenticated = (state: AuthState) => !!state.user && !!state.token;
