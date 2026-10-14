// Định nghĩa kiểu dữ liệu User & Auth cho FutaBus

export type UserRole = "Member" | "Admin" | "Driver";

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  errors?: string[];
}

export interface UserInfo {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: UserRole | string;
  hasPassword?: boolean;
}

// DTOs Xác thực (Auth)
export interface LoginRequestDto {
  username: string;
  password: string;
}

export interface LoginResponseDto {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  accessToken: string;
  refreshToken: string;
  hasPassword?: boolean;
}

export interface RegisterRequestDto {
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  password: string;
  confirmPassword: string;
}

export interface RegisterResponseDto {
  userId: number;
  username: string;
  email: string;
  fullName: string;
  role: string;
  message: string;
}

export interface RefreshTokenRequestDto {
  refreshToken: string;
}

export interface ForgotPasswordRequestDto {
  email: string;
}

export interface ResetPasswordRequestDto {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

// DTOs Hồ sơ (User Profile)
export interface UserProfileResponseDto {
  id: number;
  username: string;
  email: string;
  fullName: string;
  phoneNumber?: string;
  role: string;
  hasPassword?: boolean;
}

export interface UpdateProfileRequestDto {
  fullName: string;
  email: string;
  phoneNumber?: string;
}

export interface ChangePasswordRequestDto {
  currentPassword: string;
  newPassword: string;
  confirmNewPassword: string;
}

export interface GoogleLoginRequestDto {
  idToken: string;
}

export interface CreatePasswordRequestDto {
  newPassword: string;
  confirmNewPassword: string;
}
