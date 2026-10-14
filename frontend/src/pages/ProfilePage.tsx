// Giao diện Quản lý hồ sơ cá nhân và Đổi mật khẩu FutaBus (Dành riêng cho Khách hàng Member)
import React, { useEffect, useState } from "react";
import { User, Lock, Save, ShieldCheck } from "lucide-react";
import CustomerHeader from "../layouts/CustomerHeader";
import { useUser } from "../context/UserContext";
import { userService } from "../services/userService";
import { useToastNotifications } from "../hooks/useToastNotifications";
import { ToastNotification } from "../components/common/ToastNotification";
import styles from "./ProfilePage.module.css";

export default function ProfilePage() {
  const { user, updateUser, refreshUserProfile } = useUser();
  const { toasts, showToast, removeToast } = useToastNotifications();

  const [activeTab, setActiveTab] = useState<"profile" | "password">("profile");

  // State thông tin cá nhân
  const [profileForm, setProfileForm] = useState({
    username: "",
    fullName: "",
    email: "",
    phoneNumber: "",
  });
  const [savingProfile, setSavingProfile] = useState(false);

  // State đổi mật khẩu
  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmNewPassword: "",
  });
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    if (user) {
      setProfileForm({
        username: user.username || "",
        fullName: user.fullName || "",
        email: user.email || "",
        phoneNumber: user.phoneNumber || "",
      });
    }
  }, [user]);

  const handleUpdateProfile = async (e: React.FormEvent) => {
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

    if (profileForm.phoneNumber && !/^(0[3|5|7|8|9])[0-9]{8}$/.test(profileForm.phoneNumber.trim())) {
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

      showToast("Cập nhật thông tin hồ sơ thành công!", "success");
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Cập nhật hồ sơ thất bại.";
      showToast(msg, "error");
    } finally {
      setSavingProfile(false);
    }
  };

  const handleChangePassword = async (e: React.FormEvent) => {
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
      const msg = await userService.changePassword({
        currentPassword: isCreating ? "" : passwordForm.currentPassword,
        newPassword: passwordForm.newPassword,
        confirmNewPassword: passwordForm.confirmNewPassword,
      });

      showToast(msg || (isCreating ? "Tạo mật khẩu thành công!" : "Đổi mật khẩu thành công!"), "success");
      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmNewPassword: "",
      });
      // Refresh để cập nhật hasPassword = true
      await refreshUserProfile();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : (isCreating ? "Tạo mật khẩu thất bại." : "Đổi mật khẩu thất bại.");
      showToast(msg, "error");
    } finally {
      setSavingPassword(false);
    }
  };

  const initialLetter = user?.fullName ? user.fullName.split(" ").pop()?.charAt(0).toUpperCase() || "F" : "U";
  const hasPassword = user?.hasPassword !== false; // mặc định true nếu undefined

  return (
    <>
      <CustomerHeader />
      <ToastNotification toasts={toasts} onClose={removeToast} />

      <main className={styles.profileContainer}>
        <div className={styles.profileCard}>
          {/* Header Banner */}
          <div className={styles.profileBanner}>
            <div className={styles.avatarLarge}>{initialLetter}</div>
            <div className={styles.bannerInfo}>
              <h1 className={styles.userName}>{user?.fullName || "Người dùng FutaBus"}</h1>
              <div className={styles.userMeta}>
                <span>@{user?.username}</span>
                <span>•</span>
                <span>{user?.email}</span>
                <span className={`${styles.roleBadge} ${styles.roleMember}`}>
                  <ShieldCheck size={14} />
                  <span>Thành viên (Member)</span>
                </span>
              </div>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className={styles.tabList}>
            <button
              type="button"
              className={`${styles.tabBtn} ${activeTab === "profile" ? styles.active : ""}`}
              onClick={() => setActiveTab("profile")}
            >
              <User size={18} />
              <span>Thông tin cá nhân</span>
            </button>
            <button
              type="button"
              className={`${styles.tabBtn} ${activeTab === "password" ? styles.active : ""}`}
              onClick={() => setActiveTab("password")}
            >
              <Lock size={18} />
              <span>{hasPassword ? "Đổi mật khẩu" : "Tạo mật khẩu"}</span>
            </button>
          </div>

          {/* Tab Content */}
          <div className={styles.tabContent}>
            {activeTab === "profile" && (
              <form noValidate onSubmit={handleUpdateProfile}>
                <div className={styles.sectionTitle}>
                  <User size={18} color="#f95721" />
                  <span>Chỉnh sửa thông tin tài khoản</span>
                </div>

                <div className={styles.grid2Col}>
                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="prof-username">
                      Tên đăng nhập (Username)
                    </label>
                    <input
                      id="prof-username"
                      type="text"
                      className={styles.input}
                      value={profileForm.username}
                      disabled
                      title="Không thể đổi tên đăng nhập"
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="prof-fullname">
                      Họ và tên
                    </label>
                    <input
                      id="prof-fullname"
                      type="text"
                      className={styles.input}
                      value={profileForm.fullName}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, fullName: e.target.value })
                      }
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="prof-email">
                      Địa chỉ Email
                    </label>
                    <input
                      id="prof-email"
                      type="email"
                      className={styles.input}
                      value={profileForm.email}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, email: e.target.value })
                      }
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="prof-phone">
                      Số điện thoại
                    </label>
                    <input
                      id="prof-phone"
                      type="tel"
                      className={styles.input}
                      placeholder="0912345678"
                      value={profileForm.phoneNumber}
                      onChange={(e) =>
                        setProfileForm({ ...profileForm, phoneNumber: e.target.value })
                      }
                    />
                  </div>
                </div>

                <div className={styles.actions}>
                  <button
                    type="submit"
                    className={styles.btnPrimary}
                    disabled={savingProfile}
                  >
                    <Save size={18} />
                    <span>{savingProfile ? "Đang lưu..." : "Lưu thay đổi"}</span>
                  </button>
                </div>
              </form>
            )}

            {activeTab === "password" && (
              <form noValidate onSubmit={handleChangePassword}>
                <div className={styles.sectionTitle}>
                  <Lock size={18} color="#f95721" />
                  <span>{hasPassword ? "Thay đổi mật khẩu đăng nhập" : "Tạo mật khẩu đăng nhập"}</span>
                </div>

                <div style={{ maxWidth: "460px" }}>
                  {hasPassword && (
                    <div className={styles.formGroup}>
                      <label className={styles.label} htmlFor="pwd-current">
                        Mật khẩu hiện tại
                      </label>
                      <input
                        id="pwd-current"
                        type="password"
                        className={styles.input}
                        placeholder="Nhập mật khẩu đang dùng..."
                        value={passwordForm.currentPassword}
                        onChange={(e) =>
                          setPasswordForm({ ...passwordForm, currentPassword: e.target.value })
                        }
                      />
                    </div>
                  )}

                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="pwd-new">
                      {hasPassword ? "Mật khẩu mới" : "Mật khẩu"}
                    </label>
                    <input
                      id="pwd-new"
                      type="password"
                      className={styles.input}
                      placeholder="Tối thiểu 6 ký tự..."
                      value={passwordForm.newPassword}
                      onChange={(e) =>
                        setPasswordForm({ ...passwordForm, newPassword: e.target.value })
                      }
                    />
                  </div>

                  <div className={styles.formGroup}>
                    <label className={styles.label} htmlFor="pwd-confirm">
                      Xác nhận mật khẩu {hasPassword ? "mới" : ""}
                    </label>
                    <input
                      id="pwd-confirm"
                      type="password"
                      className={styles.input}
                      placeholder="Nhập lại mật khẩu..."
                      value={passwordForm.confirmNewPassword}
                      onChange={(e) =>
                        setPasswordForm({
                          ...passwordForm,
                          confirmNewPassword: e.target.value,
                        })
                      }
                    />
                  </div>

                  <div className={styles.actions} style={{ justifyContent: "flex-start" }}>
                    <button
                      type="submit"
                      className={styles.btnPrimary}
                      disabled={savingPassword}
                    >
                      <Lock size={18} />
                      <span>
                        {savingPassword
                          ? "Đang cập nhật..."
                          : hasPassword
                          ? "Xác nhận đổi mật khẩu"
                          : "Tạo mật khẩu"}
                      </span>
                    </button>
                  </div>
                </div>
              </form>
            )}
          </div>
        </div>
      </main>
    </>
  );
}
