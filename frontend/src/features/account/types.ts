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

export interface CreatePasswordRequestDto {
  newPassword: string;
  confirmNewPassword: string;
}
