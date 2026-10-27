import { useState, type FormEvent } from "react";
import { useUser } from "@/features/auth";
import { userService } from "@/features/account/api";
import { useToastNotifications } from "@/lib/useToastNotifications";

export function useProfile() {
  const { user, updateUser, refreshUserProfile } = useUser();
  const { toasts, showToast, removeToast } = useToastNotifications();

  // State thông tin cá nhân
  const [profileDraft, setProfileForm] = useState<{
    username: string;
    fullName: string;
    email: string;
    phoneNumber: string;
  } | null>(null);
  const profileForm = profileDraft ?? {
    username: user?.username ?? "",
    fullName: user?.fullName ?? "",
    email: user?.email ?? "",
    phoneNumber: user?.phoneNumber ?? "",
  };
  const [savingProfile, setSavingProfile] = useState(false);

  // State đổi mật khẩu
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmNewPassword: "",
  });
  const [savingPassword, setSavingPassword] = useState(false);

  const handleUpdateProfile = async (e: FormEvent) => {
    e.preventDefault();
    if (!profileForm.fullName.trim()) {
      showToast("Vui lòng nhập họ và tên.", "error");
      return;
    }

    if (!profileForm.email.trim()) {
      showToast("Vui lòng nhập địa chỉ email.", "error");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.email.trim())) {
      showToast("Địa chỉ email không đúng định dạng.", "error");
      return;
    }

    if (profileForm.phoneNumber && !/^0[35789][0-9]{8}$/.test(profileForm.phoneNumber.trim())) {
      showToast("Số điện thoại phải có 10 chữ số chuẩn Việt Nam (03x, 05x, 07x, 08x, 09x).", "error");
      return;
    }

    setSavingProfile(true);
    try {
      const res = await userService.updateProfile({
        fullName: profileForm.fullName.trim(),
        email: profileForm.email.trim(),
        phoneNumber: profileForm.phoneNumber.trim() || undefined,
      });

      updateUser({
        fullName: res.fullName,
        email: res.email,
        phoneNumber: res.phoneNumber,
      });
      setProfileForm(null);

      showToast("Cập nhật thông tin hồ sơ thành công!", "success");
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Cập nhật hồ sơ thất bại.";
      showToast(msg, "error");
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();

    const isCreating = user?.hasPassword === false;

    if (!isCreating && !passwordForm.currentPassword) {
      showToast("Vui lòng nhập mật khẩu hiện tại.", "error");
      return;
    }

    if (!passwordForm.newPassword) {
      showToast("Vui lòng nhập mật khẩu mới.", "error");
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      showToast("Mật khẩu mới phải có tối thiểu 6 ký tự.", "error");
      return;
    }

    if (!passwordForm.confirmNewPassword) {
      showToast("Vui lòng nhập lại mật khẩu xác nhận.", "error");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
      showToast("Mật khẩu xác nhận mới không trùng khớp.", "error");
      return;
    }

    setSavingPassword(true);
    try {
      await userService.changePassword({
        currentPassword: isCreating ? "" : passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmNewPassword: passwordForm.confirmNewPassword,
      });

      showToast(isCreating ? "Tạo mật khẩu thành công!" : "Đổi mật khẩu thành công!", "success");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmNewPassword: "",
      });
      // Refresh để cập nhật hasPassword = true
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : isCreating ? "Tạo mật khẩu thất bại." : "Đổi mật khẩu thất bại.";
      showToast(msg, "error");
    } finally {
      setSavingPassword(false);
    }
  };

  const initialLetter = user?.fullName ? user.fullName.split(" ").pop()?.charAt(0).toUpperCase() || "F" : "U";
  const hasPassword = user?.hasPassword !== false; // mặc định true nếu undefined

  return {
    user,
    toasts,
    removeToast,
    profileForm,
    setProfileForm,
    savingProfile,
    passwordForm,
    setPasswordForm,
    savingPassword,
    handleUpdateProfile,
    handleChangePassword,
    initialLetter,
    hasPassword,
  };
}
