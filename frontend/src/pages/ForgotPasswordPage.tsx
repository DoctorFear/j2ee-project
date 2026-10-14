// Giao diện Yêu cầu đặt lại mật khẩu (Quên mật khẩu)
import React, { useState } from "react";
import { Link } from "react-router-dom";
import { BusFront, Mail, ArrowLeft, Send } from "lucide-react";
import { useAuthAction } from "../hooks/useAuthAction";
import { useToastNotifications } from "../hooks/useToastNotifications";
import { ToastNotification } from "../components/common/ToastNotification";
import styles from "./LoginPage.module.css";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [submitted, setSubmitted] = useState(false);

  const { toasts, showToast, removeToast } = useToastNotifications();
  const { submitting, handleForgotPassword } = useAuthAction(showToast);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim()) {
      showToast("Vui lòng nhập địa chỉ email tài khoản của bạn.", "error");
      return;
    }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) {
      showToast("Địa chỉ email không đúng định dạng.", "error");
      return;
    }
    const success = await handleForgotPassword({ email: email.trim() });
    if (success) {
      setSubmitted(true);
    }
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
              <div className={styles.brandSub}>Khôi phục tài khoản</div>
            </div>
          </Link>
          <h1 className={styles.authTitle}>Quên mật khẩu?</h1>
          <p className={styles.authSubtitle}>
            Nhập email đã đăng ký tài khoản FutaBus để nhận mã xác nhận đặt lại mật khẩu mới.
          </p>
        </div>

        {submitted ? (
          <div style={{ textAlign: "center", padding: "16px 0" }}>
            <div
              style={{
                width: "56px",
                height: "56px",
                background: "#ecfdf5",
                color: "#059669",
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                margin: "0 auto 16px",
              }}
            >
              <Mail size={28} />
            </div>
            <h3 style={{ fontSize: "17px", fontWeight: 700, color: "#0f172a", marginBottom: "8px" }}>
              Đã gửi liên kết khôi phục!
            </h3>
            <p style={{ fontSize: "13.5px", color: "#64748b", lineHeight: 1.5, marginBottom: "24px" }}>
              Vui lòng kiểm tra hòm thư đến của <strong>{email}</strong> (kể cả thư rác/spam) và làm theo hướng dẫn.
            </p>
            <Link to="/login" className={styles.submitBtn} style={{ textDecoration: "none" }}>
              <ArrowLeft size={18} />
              <span>Quay lại trang Đăng nhập</span>
            </Link>
          </div>
        ) : (
          <form noValidate onSubmit={handleSubmit}>
            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="forgot-email">
                <span>Email đăng ký <span className={styles.required}>*</span></span>
              </label>
              <div className={styles.inputWrapper}>
                <Mail size={18} className={styles.inputIcon} />
                <input
                  id="forgot-email"
                  type="email"
                  className={styles.input}
                  placeholder="nhap-email@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
            </div>

            <button
              type="submit"
              className={styles.submitBtn}
              disabled={submitting}
              style={{ marginTop: "14px" }}
            >
              {submitting ? (
                <span>Đang gửi mã...</span>
              ) : (
                <>
                  <Send size={18} />
                  <span>Gửi liên kết đặt lại mật khẩu</span>
                </>
              )}
            </button>

            <div className={styles.footerText}>
              <Link to="/login" className={styles.forgotLink} style={{ display: "inline-flex", alignItems: "center", gap: "6px" }}>
                <ArrowLeft size={16} />
                <span>Quay lại Đăng nhập</span>
              </Link>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
