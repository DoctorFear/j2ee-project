// Context quản lý trạng thái đăng nhập và người dùng toàn cục cho FutaBus
import React, { createContext, useContext, useEffect, useState } from "react";
import type { LoginResponseDto, UserInfo, UserProfileResponseDto } from "../types/user";
import { clearTokens, getAccessToken } from "../services/apiClient";
import { authService } from "../services/authService";
import { userService } from "../services/userService";

interface UserContextType {
  user: UserInfo | null;
  token: string | null;
  isAuthenticated: boolean;
  loading: boolean;
  loginSuccess: (loginData: LoginResponseDto) => void;
  logoutClient: () => Promise<void>;
  updateUser: (updated: Partial<UserInfo>) => void;
  refreshUserProfile: () => Promise<void>;
}

const UserContext = createContext<UserContextType | undefined>(undefined);

export const UserProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [token, setToken] = useState<string | null>(getAccessToken());
  const [loading, setLoading] = useState<boolean>(true);

  // Nạp lại hồ sơ người dùng nếu đã có token
  const refreshUserProfile = async () => {
    try {
      const profile: UserProfileResponseDto = await userService.getProfile();
      setUser({
        id: profile.id,
        username: profile.username,
        email: profile.email,
        fullName: profile.fullName,
        phoneNumber: profile.phoneNumber,
        role: profile.role,
        hasPassword: profile.hasPassword ?? true,
      });
    } catch (err) {
      console.warn("Không thể tải thông tin người dùng từ token:", err);
      clearTokens();
      setUser(null);
      setToken(null);
    }
  };

  useEffect(() => {
    const initializeAuth = async () => {
      const savedToken = getAccessToken();
      if (savedToken) {
        setToken(savedToken);
        await refreshUserProfile();
      }
      setLoading(false);
    };

    initializeAuth();

    // Lắng nghe sự kiện hết hạn token từ apiClient
    const handleAuthExpired = () => {
      setUser(null);
      setToken(null);
    };

    window.addEventListener("futa-auth-expired", handleAuthExpired);
    return () => {
      window.removeEventListener("futa-auth-expired", handleAuthExpired);
    };
  }, []);

  const loginSuccess = (loginData: LoginResponseDto) => {
    setToken(loginData.accessToken);
    setUser({
      id: loginData.userId,
      username: loginData.username,
      email: loginData.email,
      fullName: loginData.fullName,
      role: loginData.role,
      hasPassword: loginData.hasPassword ?? true,
    });
  };

  const logoutClient = async () => {
    try {
      await authService.logout();
    } finally {
      clearTokens();
      setUser(null);
      setToken(null);
    }
  };

  const updateUser = (updated: Partial<UserInfo>) => {
    setUser((prev) => (prev ? { ...prev, ...updated } : null));
  };

  return (
    <UserContext.Provider
      value={{
        user,
        token,
        isAuthenticated: !!user && !!token,
        loading,
        loginSuccess,
        logoutClient,
        updateUser,
        refreshUserProfile,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser phải được sử dụng bên trong UserProvider");
  }
  return context;
};
