import { apiClient } from "@/api/client";
import type { UserProfileResponseDto } from "@/features/auth";
import type { ChangePasswordRequestDto, UpdateProfileRequestDto } from "./types";

export const userService = {
  updateProfile: (data: UpdateProfileRequestDto) =>
    apiClient<UserProfileResponseDto>("/api/user/profile", { method: "PUT", data }),
  changePassword: (data: ChangePasswordRequestDto) =>
    apiClient<boolean>("/api/user/change-password", { method: "POST", data }),
};
