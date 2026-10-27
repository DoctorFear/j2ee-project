import { useProfile } from "@/features/account/useProfile";
// Giao diện Quản lý hồ sơ cá nhân và Đổi mật khẩu FutaBus (Dành riêng cho Khách hàng Member)
import React, { useState } from "react";
import { User, Lock, Save, ShieldCheck } from "lucide-react";
import { ToastNotification } from "@/components/shared/ToastNotification";
import styles from "@/features/account/ProfilePage.module.css";

export default function ProfilePage({ header }: { header: React.ReactNode }) {
  const [activeTab, setActiveTab] = useState<"profile" | "password">("profile");
  const {
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
  } = useProfile();

  return (
    <>
      {header}
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
                      onChange={(e) => setProfileForm({ ...profileForm, fullName: e.target.value })}
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
                      onChange={(e) => setProfileForm({ ...profileForm, email: e.target.value })}
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
                      onChange={(e) => setProfileForm({ ...profileForm, phoneNumber: e.target.value })}
                    />
                  </div>
                </div>

                <div className={styles.actions}>
                  <button type="submit" className={styles.btnPrimary} disabled={savingProfile}>
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
                        onChange={(e) => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
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
                      onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
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
                    <button type="submit" className={styles.btnPrimary} disabled={savingPassword}>
                      <Lock size={18} />
                      <span>
                        {savingPassword ? "Đang cập nhật..." : hasPassword ? "Xác nhận đổi mật khẩu" : "Tạo mật khẩu"}
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
