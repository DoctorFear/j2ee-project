// Giao diện Đăng nhập FutaBus
import React, { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { BusFront, User, Lock, Eye, EyeOff, ArrowRight } from "lucide-react";
import { GoogleLogin } from "@react-oauth/google";
import { useUser } from "../context/UserContext";
import { useAuthAction } from "../hooks/useAuthAction";
import { useToastNotifications } from "../hooks/useToastNotifications";
import { ToastNotification } from "../components/common/ToastNotification";
import { authService } from "../services/authService";
import styles from "./LoginPage.module.css";

export default function LoginPage() {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);

  const { toasts, showToast, removeToast } = useToastNotifications();
  const { submitting, handleLogin } = useAuthAction(showToast);
  const { user, isAuthenticated, loginSuccess } = useUser();
  const navigate = useNavigate();

  const location = useLocation();
  const from = (location.state as { from?: { pathname: string } })?.from?.pathname || "/";

  // Nếu đã đăng nhập, tự động chuyển về phân hệ chức năng tương ứng
  if (isAuthenticated && user) {
    if (user.role === "Admin") return <Navigate to="/admin/routes" replace />;
    if (user.role === "Driver") return <Navigate to="/driver/trips" replace />;
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) {
      showToast("Vui lòng nhập tên đăng nhập hoặc email.", "error");
      return;
    }
    if (!password) {
      showToast("Vui lòng nhập mật khẩu.", "error");
      return;
    }
    await handleLogin({ username: username.trim(), password }, from);
  };

  const fillDemo = (u: string, p: string) => {
    setUsername(u);
    setPassword(p);
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
              <div className={styles.brandSub}>Chất lượng là danh dự</div>
            </div>
          </Link>
          <h1 className={styles.authTitle}>Đăng nhập tài khoản</h1>
          <p className={styles.authSubtitle}>Nhập thông tin để quản lý vé và chuyến đi của bạn</p>
        </div>

        <form noValidate onSubmit={handleSubmit}>
          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="login-username">
              <span>Tên đăng nhập <span className={styles.required}>*</span></span>
            </label>
            <div className={styles.inputWrapper}>
              <User size={18} className={styles.inputIcon} />
              <input
                id="login-username"
                type="text"
                className={styles.input}
                placeholder="Nhập tên đăng nhập..."
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </div>
          </div>

          <div className={styles.formGroup}>
            <label className={styles.label} htmlFor="login-password">
              <span>Mật khẩu <span className={styles.required}>*</span></span>
            </label>
            <div className={styles.inputWrapper}>
              <Lock size={18} className={styles.inputIcon} />
              <input
                id="login-password"
                type={showPassword ? "text" : "password"}
                className={styles.input}
                placeholder="Nhập mật khẩu..."
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
              <button
                type="button"
                className={styles.eyeButton}
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
              >
                {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
          </div>

          <div className={styles.formOptions}>
            <label className={styles.rememberMe}>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
              />
              <span>Ghi nhớ đăng nhập</span>
            </label>

            <Link to="/forgot-password" className={styles.forgotLink}>
              Quên mật khẩu?
            </Link>
          </div>

          <button
            type="submit"
            className={styles.submitBtn}
            disabled={submitting}
          >
            {submitting ? (
              <span>Đang kiểm tra...</span>
            ) : (
              <>
                <span>Đăng nhập</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        {/* Divider */}
        <div style={{ display: "flex", alignItems: "center", gap: "12px", margin: "20px 0" }}>
          <div style={{ flex: 1, height: "1px", background: "#e2e8f0" }} />
          <span style={{ fontSize: "12.5px", color: "#94a3b8", fontWeight: 600, whiteSpace: "nowrap" }}>
            hoặc đăng nhập bằng
          </span>
          <div style={{ flex: 1, height: "1px", background: "#e2e8f0" }} />
        </div>

        {/* Google Login */}
        <div style={{ display: "flex", justifyContent: "center" }}>
          <GoogleLogin
            onSuccess={async (credentialResponse) => {
              if (!credentialResponse.credential) return;
              try {
                const data = await authService.googleLogin({ idToken: credentialResponse.credential });
                loginSuccess(data);
                if (data.role === "Admin") navigate("/admin/routes", { replace: true });
                else if (data.role === "Driver") navigate("/driver/trips", { replace: true });
                else navigate("/", { replace: true });
              } catch (err) {
                const msg = err instanceof Error ? err.message : "Đăng nhập Google thất bại.";
                showToast(msg, "error");
              }
            }}
            onError={() => showToast("Đăng nhập Google bị từ chối.", "error")}
            useOneTap={false}
            width="100%"
            text="signin_with"
            shape="rectangular"
          />
        </div>

        <div className={styles.demoBox}>
          <div className={styles.demoTitle}>
            <span>⚡ Tài khoản thử nghiệm nhanh (3 vai trò):</span>
          </div>
          <div className={styles.demoItem}>
            <span>Quản trị (Admin):</span>
            <button
              type="button"
              className={styles.demoBadge}
              onClick={() => fillDemo("admin", "Admin@123")}
              title="Bấm để điền nhanh"
            >
              admin / Admin@123
            </button>
          </div>
          <div className={styles.demoItem} style={{ marginTop: "6px" }}>
            <span>Thành viên (Member):</span>
            <button
              type="button"
              className={styles.demoBadge}
              onClick={() => fillDemo("member1", "Member@123")}
              title="Bấm để điền nhanh"
            >
              member1 / Member@123
            </button>
          </div>
          <div className={styles.demoItem} style={{ marginTop: "6px" }}>
            <span>Tài xế / Shiper (Driver):</span>
            <button
              type="button"
              className={styles.demoBadge}
              onClick={() => fillDemo("driver1", "Driver@123")}
              title="Bấm để điền nhanh"
            >
              driver1 / Driver@123
            </button>
          </div>
        </div>

        <div className={styles.footerText}>
          Chưa có tài khoản FutaBus?
          <Link to="/register" className={styles.footerLink}>
            Đăng ký ngay
          </Link>
        </div>
      </div>
    </div>
  );
}
