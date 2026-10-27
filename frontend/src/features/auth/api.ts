import { apiClient } from "@/api/client";
import type {
  ForgotPasswordRequestDto,
  RegisterRequestDto,
  RegisterResponseDto,
  ResetPasswordRequestDto,
} from "./types";

export const authFormsApi = {
  register: (data: RegisterRequestDto) =>
    apiClient<RegisterResponseDto>("/api/auth/register", {
      method: "POST",
      data,
      authMode: "none",
    }),
  forgotPassword: (data: ForgotPasswordRequestDto) =>
    apiClient<boolean>("/api/auth/forgot-password", { method: "POST", data, authMode: "none" }),
  resetPassword: (data: ResetPasswordRequestDto) =>
    apiClient<boolean>("/api/auth/reset-password", { method: "POST", data, authMode: "none" }),
};
