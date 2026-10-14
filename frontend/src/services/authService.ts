// Dịch vụ gọi API Xác thực (Auth) cho FutaBus
import { apiClient, clearTokens, getRefreshToken, setTokens } from "./apiClient";
import type {
  ForgotPasswordRequestDto,
  GoogleLoginRequestDto,
  LoginRequestDto,
  LoginResponseDto,
  RefreshTokenRequestDto,
  RegisterRequestDto,
  RegisterResponseDto,
  ResetPasswordRequestDto,
} from "../types/user";

export const authService = {
  // Đăng nhập
  async login(payload: LoginRequestDto): Promise<LoginResponseDto> {
    const data = await apiClient<LoginResponseDto>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload),
    }, false);

    if (data.accessToken && data.refreshToken) {
      setTokens(data.accessToken, data.refreshToken);
    }
    return data;
  },

  // Đăng ký tài khoản khách hàng mới
  async register(payload: RegisterRequestDto): Promise<RegisterResponseDto> {
    return await apiClient<RegisterResponseDto>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload),
    }, false);
  },

  // Làm mới access token
  async refreshToken(payload: RefreshTokenRequestDto): Promise<{ accessToken: string; refreshToken: string }> {
    const data = await apiClient<{ accessToken: string; refreshToken: string }>("/api/auth/refresh-token", {
      method: "POST",
      body: JSON.stringify(payload),
    }, false);

    if (data.accessToken && data.refreshToken) {
      setTokens(data.accessToken, data.refreshToken);
    }
    return data;
  },

  // Quên mật khẩu
  async forgotPassword(payload: ForgotPasswordRequestDto): Promise<string> {
    return await apiClient<string>("/api/auth/forgot-password", {
      method: "POST",
      body: JSON.stringify(payload),
    }, false);
  },

  // Đặt lại mật khẩu mới
  async resetPassword(payload: ResetPasswordRequestDto): Promise<string> {
    return await apiClient<string>("/api/auth/reset-password", {
      method: "POST",
      body: JSON.stringify(payload),
    }, false);
  },

  // Đăng nhập bằng Google OAuth
  async googleLogin(payload: GoogleLoginRequestDto): Promise<LoginResponseDto> {
    const data = await apiClient<LoginResponseDto>('/api/auth/google-login', {
      method: 'POST',
      body: JSON.stringify(payload),
    }, false);
    if (data.accessToken && data.refreshToken) {
      setTokens(data.accessToken, data.refreshToken);
    }
    return data;
  },

  // Đăng xuất
  async logout(): Promise<void> {
    const refreshToken = getRefreshToken();
    try {
      if (refreshToken) {
        await apiClient<void>("/api/auth/logout", {
          method: "POST",
          body: JSON.stringify({ refreshToken }),
        }, false);
      }
    } catch (err) {
      console.warn("Lỗi khi gọi API logout:", err);
    } finally {
      clearTokens();
    }
  },
};
