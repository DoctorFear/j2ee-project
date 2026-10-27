// Giao diện Đặt lại mật khẩu mới qua token
import React, { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { BusFront, Lock, KeyRound, Eye, EyeOff, CheckCircle2, ArrowLeft } from "lucide-react";
import { useAuthAction } from "@/features/auth/useAuthAction";
import { useToastNotifications } from "@/lib/useToastNotifications";
import { ToastNotification } from "@/components/shared/ToastNotification";
import styles from "@/features/auth/pages/LoginPage.module.css";

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const tokenFromUrl = searchParams.get("token") || "";

  const [token, setToken] = useState(tokenFromUrl);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const { toasts, showToast, removeToast } = useToastNotifications();
  const { submitting, handleResetPassword } = useAuthAction(showToast);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!token.trim()) {
      showToast("Thiếu mã xác nhận đặt lại mật khẩu (Token).", "error");
      return;
    }

    if (!newPassword) {
      showToast("Vui lòng nhập mật khẩu mới.", "error");
      return;
    }

    if (newPassword.length < 6) {
      showToast("Mật khẩu mới phải có ít nhất 6 ký tự.", "error");
      return;
    }

    if (!confirmPassword) {
      showToast("Vui lòng nhập lại mật khẩu xác nhận.", "error");
      return;
    }

    if (newPassword !== confirmPassword) {
      showToast("Mật khẩu xác nhận không trùng khớp.", "error");
      return;
    }

    await handleResetPassword({
      token: token.trim(),
      newPassword,
      confirmPassword,
    });
  };

  return (
    <div className={styles.authContainer}>
      <ToastNotification toasts={toasts} onClose={removeToast} />

      <div className={styles.authCard}>
        <div className={styles.authHeader}>
          <Link to="/" className={styles.logoWrapper}>
            <div className={styles.logoIcon}>
              <BusFront size={24} />
            </div>
            <div className={styles.logoText}>
              <div className={styles.brandName}>FUTA Bus Lines</div>
              <div className={styles.brandSub}>Bảo mật tài khoản</div>
            </div>
          </Link>
          <h1 className={styles.authTitle}>Đặt lại mật khẩu mới</h1>
          <p className={styles.authSubtitle}>Thiết lập mật khẩu an toàn mới cho tài khoản FutaBus của bạn</p>
        </div>

        <form noValidate onSubmit={handleSubmit}>
          {!tokenFromUrl && (
            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reset-token">
                <span>
                  Mã token xác thực <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <KeyRound size={18} className={styles.inputIcon} />
                <input
                  id="reset-token"
                  type="text"
                  className={styles.input}
                  placeholder="Dán mã token được gửi qua email..."
                  value={token}
                  onChange={(e) => setToken(e.target.value)}
                />
              </div>
            </div>
          )}

          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="reset-new-password">
              <span>
                Mật khẩu mới <span className={styles.required}>*</span>
              </span>
            </label>
            <div className={styles.inputWrapper}>
              <Lock size={18} className={styles.inputIcon} />
              <input
                id="reset-new-password"
                type={showNewPassword ? "text" : "password"}
                className={styles.input}
                placeholder="Tối thiểu 6 ký tự..."
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
              <button
                type="button"
                className={styles.eyeButton}
                onClick={() => setShowNewPassword(!showNewPassword)}
                aria-label={showNewPassword ? "Ẩn" : "Hiện"}
              >
                {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="reset-confirm-password">
              <span>
                Xác nhận mật khẩu mới <span className={styles.required}>*</span>
              </span>
            </label>
            <div className={styles.inputWrapper}>
              <Lock size={18} className={styles.inputIcon} />
              <input
                id="reset-confirm-password"
                type={showConfirmPassword ? "text" : "password"}
                className={styles.input}
                placeholder="Nhập lại mật khẩu mới..."
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <button
                type="button"
                className={styles.eyeButton}
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                aria-label={showConfirmPassword ? "Ẩn" : "Hiện"}
              >
                {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <button type="submit" className={styles.submitBtn} disabled={submitting} style={{ marginTop: "14px" }}>
            {submitting ? (
              <span>Đang lưu mật khẩu...</span>
            ) : (
              <>
                <CheckCircle2 size={18} />
                <span>Cập nhật mật khẩu mới</span>
              </>
            )}
          </button>
        </form>

        <div className={styles.footerText}>
          <Link
            to="/login"
            className={styles.forgotLink}
            style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}
          >
            <ArrowLeft size={16} />
            <span>Quay lại Đăng nhập</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
