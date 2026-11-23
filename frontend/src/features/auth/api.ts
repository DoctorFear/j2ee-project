import { apiClient } from "@/api/client";
import type {
  ForgotPasswordRequestDto,
  RegisterRequestDto,
  RegisterResponseDto,
  ResetPasswordRequestDto,
} from "./types";

export const authFormsApi = {
  register: (data: RegisterRequestDto) =>
    apiClient<RegisterResponseDto>("/auth/register", {
      method: "POST",
      data,
      authMode: "none",
    }),
  forgotPassword: (data: ForgotPasswordRequestDto) =>
    apiClient<boolean>("/auth/forgot-password", { method: "POST", data, authMode: "none" }),
  resetPassword: (data: ResetPasswordRequestDto) =>
    apiClient<boolean>("/auth/reset-password", { method: "POST", data, authMode: "none" }),
};
