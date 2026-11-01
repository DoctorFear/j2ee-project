// Giao diện Đăng ký tài khoản khách hàng FutaBus
import React, { useState } from "react";
import { Link, Navigate } from "react-router-dom";
import { BusFront, User, Mail, Phone, Lock, Eye, EyeOff, UserPlus } from "lucide-react";
import { useUser } from "../useUser";
import { useAuthAction } from "@/features/auth/useAuthAction";
import { useToast} from "@/components/shared/toast/toast-context.ts";
import styles from "@/features/auth/pages/LoginPage.module.css";

export default function RegisterPage() {
  const { user, isAuthenticated } = useUser();

  const [formData, setFormData] = useState({
    username: "",
    fullName: "",
    email: "",
    phoneNumber: "",
    password: "",
    confirmPassword: "",
  });

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const toast = useToast();
  const { submitting, handleRegister } = useAuthAction(toast);

  if (isAuthenticated && user) {
    if (user.role === "Admin") return <Navigate to="/admin/routes" replace />;
    if (user.role === "Driver") return <Navigate to="/driver/trips" replace />;
    return <Navigate to="/" replace />;
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.username.trim()) {
      toast.error("Vui lòng nhập tên đăng nhập.");
      return;
    }

    if (formData.username.trim().length < 3) {
      toast.error("Tên đăng nhập phải có ít nhất 3 ký tự.");
      return;
    }

    if (!formData.fullName.trim()) {
      toast.error("Vui lòng nhập họ và tên.");
      return;
    }

    if (!formData.email.trim()) {
      toast.error("Vui lòng nhập địa chỉ email.");
      return;
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      toast.error("Địa chỉ email không đúng định dạng (ví dụ: ten@gmail.com).");
      return;
    }

    if (formData.phoneNumber && !/^0[35789][0-9]{8}$/.test(formData.phoneNumber.trim())) {
      toast.error("Số điện thoại phải có 10 chữ số chuẩn Việt Nam (03x, 05x, 07x, 08x, 09x).");
      return;
    }

    if (!formData.password) {
      toast.error("Vui lòng nhập mật khẩu.");
      return;
    }

    if (formData.password.length < 6) {
      toast.error("Mật khẩu phải có độ dài tối thiểu 6 ký tự.");
      return;
    }

    if (!formData.confirmPassword) {
      toast.error("Vui lòng nhập lại mật khẩu xác nhận.");
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      toast.error("Mật khẩu xác nhận không trùng khớp.");
      return;
    }

    await handleRegister({
      username: formData.username.trim(),
      fullName: formData.fullName.trim(),
      email: formData.email.trim(),
      phoneNumber: formData.phoneNumber.trim() || undefined,
      password: formData.password,
      confirmPassword: formData.confirmPassword,
    });
  };

  return (
    <div className={styles.authContainer}>

      <div className={`${styles.authCard} ${styles.authCardWide}`}>
        <div className={styles.authHeader}>
          <Link to="/" className={styles.logoWrapper}>
            <div className={styles.logoIcon}>
              <BusFront size={24} />
            </div>
            <div className={styles.logoText}>
              <div className={styles.brandName}>FUTA Bus Lines</div>
              <div className={styles.brandSub}>Đăng ký thành viên</div>
            </div>
          </Link>
          <h1 className={styles.authTitle}>Tạo tài khoản mới</h1>
          <p className={styles.authSubtitle}>Đăng ký ngay để đặt vé nhanh chóng và nhận nhiều ưu đãi</p>
        </div>

        <form noValidate onSubmit={handleSubmit}>
          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-username">
                <span>
                  Tên đăng nhập <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <User size={18} className={styles.inputIcon} />
                <input
                  id="reg-username"
                  name="username"
                  type="text"
                  className={styles.input}
                  placeholder="Ví dụ: nguyenvana"
                  value={formData.username}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-fullname">
                <span>
                  Họ và tên <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <User size={18} className={styles.inputIcon} />
                <input
                  id="reg-fullname"
                  name="fullName"
                  type="text"
                  className={styles.input}
                  placeholder="Ví dụ: Nguyễn Văn A"
                  value={formData.fullName}
                  onChange={handleChange}
                />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-email">
                <span>
                  Email <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <Mail size={18} className={styles.inputIcon} />
                <input
                  id="reg-email"
                  name="email"
                  type="email"
                  className={styles.input}
                  placeholder="email@example.com"
                  value={formData.email}
                  onChange={handleChange}
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-phone">
                <span>Số điện thoại</span>
              </label>
              <div className={styles.inputWrapper}>
                <Phone size={18} className={styles.inputIcon} />
                <input
                  id="reg-phone"
                  name="phoneNumber"
                  type="tel"
                  className={styles.input}
                  placeholder="0912345678"
                  value={formData.phoneNumber}
                  onChange={handleChange}
                />
              </div>
            </div>
          </div>

          <div className={styles.formRow}>
            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-password">
                <span>
                  Mật khẩu <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <Lock size={18} className={styles.inputIcon} />
                <input
                  id="reg-password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  className={styles.input}
                  placeholder="Tối thiểu 6 ký tự..."
                  value={formData.password}
                  onChange={handleChange}
                />
                <button
                  type="button"
                  className={styles.eyeButton}
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Ẩn" : "Hiện"}
                >
                  {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label} htmlFor="reg-confirm">
                <span>
                  Xác nhận mật khẩu <span className={styles.required}>*</span>
                </span>
              </label>
              <div className={styles.inputWrapper}>
                <Lock size={18} className={styles.inputIcon} />
                <input
                  id="reg-confirm"
                  name="confirmPassword"
                  type={showConfirmPassword ? "text" : "password"}
                  className={styles.input}
                  placeholder="Nhập lại mật khẩu..."
                  value={formData.confirmPassword}
                  onChange={handleChange}
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
          </div>

          <button type="submit" className={styles.submitBtn} disabled={submitting} style={{ marginTop: "12px" }}>
            {submitting ? (
              <span>Đang xử lý đăng ký...</span>
            ) : (
              <>
                <UserPlus size={18} />
                <span>Hoàn tất đăng ký</span>
              </>
            )}
          </button>
        </form>

        <div className={styles.footerText}>
          Đã có tài khoản FutaBus?
          <Link to="/login" className={styles.footerLink}>
            Đăng nhập ngay
          </Link>
        </div>
      </div>
    </div>
  );
}
