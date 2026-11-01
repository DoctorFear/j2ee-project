import { useState, type FormEvent } from "react";
import { useUser } from "@/features/auth";
import { userService } from "@/features/account/api";
import { useToast } from "@/components/shared/toast/toast-context";

export function useProfile() {
  const { user, updateUser, refreshUserProfile } = useUser();
  const toast = useToast();

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
      toast.error("Vui lòng nhập họ và tên.");
      return;
    }

    if (!profileForm.email.trim()) {
      toast.error("Vui lòng nhập địa chỉ email.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profileForm.email.trim())) {
      toast.error("Địa chỉ email không đúng định dạng.");
      return;
    }

    if (profileForm.phoneNumber && !/^0[35789][0-9]{8}$/.test(profileForm.phoneNumber.trim())) {
      toast.error("Số điện thoại phải có 10 chữ số chuẩn Việt Nam (03x, 05x, 07x, 08x, 09x).");
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

      toast.success("Cập nhật thông tin hồ sơ thành công!");
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Cập nhật hồ sơ thất bại.";
      toast.error(msg);
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: FormEvent) => {
    e.preventDefault();

    const isCreating = user?.hasPassword === false;

    if (!isCreating && !passwordForm.currentPassword) {
      toast.error("Vui lòng nhập mật khẩu hiện tại.");
      return;
    }

    if (!passwordForm.newPassword) {
      toast.error("Vui lòng nhập mật khẩu mới.");
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      toast.error("Mật khẩu mới phải có tối thiểu 6 ký tự.");
      return;
    }

    if (!passwordForm.confirmNewPassword) {
      toast.error("Vui lòng nhập lại mật khẩu xác nhận.");
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmNewPassword) {
      toast.error("Mật khẩu xác nhận mới không trùng khớp.");
      return;
    }

    setSavingPassword(true);
    try {
      await userService.changePassword({
        currentPassword: isCreating ? "" : passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmNewPassword: passwordForm.confirmNewPassword,
      });

      toast.success( isCreating? "Tạo mật khẩu thành công" : "Đổi mật khẩu thành công!");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmNewPassword: ""
      });
      // Refresh để cập nhật hasPassword = true
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : isCreating ? "Tạo mật khẩu thất bại." : "Đổi mật khẩu thất bại.";
      toast.error(msg);
    } finally {
      setSavingPassword(false);
    }
  };

  const initialLetter = user?.fullName ? user.fullName.split(" ").pop()?.charAt(0).toUpperCase() || "F" : "U";
  const hasPassword = user?.hasPassword !== false; // mặc định true nếu undefined

  return {
    user,
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
