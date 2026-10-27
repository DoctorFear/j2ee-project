import { z } from "zod";
import { apiClient } from "@/api/client";
import { clearTokens, getAccessToken, getRefreshToken } from "@/api/session";
import type { GoogleLoginRequestDto, LoginRequestDto, LoginResponseDto, UserProfileResponseDto } from "./types";

const profileSchema = z.object({
  id: z.number(),
  username: z.string(),
  email: z.string(),
  fullName: z.string(),
  phoneNumber: z
    .string()
    .nullish()
    .transform((value) => value ?? undefined),
  role: z.string(),
  hasPassword: z.boolean().optional(),
});
const loginSchema = profileSchema.omit({ id: true, phoneNumber: true }).extend({
  userId: z.number(),
  accessToken: z.string().min(1),
  refreshToken: z.string().min(1),
});

export const sessionApi = {
  async login(payload: LoginRequestDto): Promise<LoginResponseDto> {
    return loginSchema.parse(await apiClient("/api/auth/login", { method: "POST", data: payload, authMode: "none" }));
  },
  async googleLogin(payload: GoogleLoginRequestDto): Promise<LoginResponseDto> {
    return loginSchema.parse(
      await apiClient("/api/auth/google-login", { method: "POST", data: payload, authMode: "none" })
    );
  },
  async getProfile(): Promise<UserProfileResponseDto> {
    return profileSchema.parse(await apiClient("/api/user/profile"));
  },
  async logout(): Promise<void> {
    const accessToken = getAccessToken();
    const refreshToken = getRefreshToken();
    // Clear synchronously so pending refresh/profile responses cannot revive this session.
    clearTokens();
    if (accessToken)
      await apiClient("/api/auth/logout", {
        method: "POST",
        data: { refreshToken },
        authMode: "none",
        headers: { Authorization: `Bearer ${accessToken}` },
      });
  },
};
