// Dịch vụ gọi API Thông tin tài khoản người dùng (User)
import { apiClient } from "./apiClient";
import type {
  ChangePasswordRequestDto,
  UpdateProfileRequestDto,
  UserProfileResponseDto,
} from "../types/user";

export const userService = {
  // Lấy thông tin cá nhân hiện tại
  async getProfile(): Promise<UserProfileResponseDto> {
    return await apiClient<UserProfileResponseDto>("/api/user/profile", {
      method: "GET",
    });
  },

  // Cập nhật họ tên, email, số điện thoại
  async updateProfile(payload: UpdateProfileRequestDto): Promise<UserProfileResponseDto> {
    return await apiClient<UserProfileResponseDto>("/api/user/profile", {
      method: "PUT",
      body: JSON.stringify(payload),
    });
  },

  // Đổi mật khẩu
  async changePassword(payload: ChangePasswordRequestDto): Promise<string> {
    return await apiClient<string>("/api/user/change-password", {
      method: "POST",
      body: JSON.stringify(payload),
    });
  },
};
