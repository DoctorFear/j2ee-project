// Hook điều phối toàn bộ nghiệp vụ Xác thực (Auth) cho giao diện FutaBus
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "../context/UserContext";
import { authService } from "../services/authService";
import type {
  ForgotPasswordRequestDto,
  LoginRequestDto,
  RegisterRequestDto,
  ResetPasswordRequestDto,
} from "../types/user";

export function useAuthAction(showToast?: (msg: string, type: "success" | "error" | "info") => void) {
  const { loginSuccess, logoutClient } = useUser();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Xử lý đăng nhập
  const handleLogin = async (payload: LoginRequestDto, returnUrl?: string) => {
    setSubmitting(true);
    try {
      const res = await authService.login(payload);
      loginSuccess(res);
      showToast?.("Đăng nhập thành công! Chào mừng quý khách.", "success");

      if (res.role === "Admin") {
        navigate(returnUrl && returnUrl.startsWith("/admin") ? returnUrl : "/admin/routes", { replace: true });
      } else if (res.role === "Driver") {
        navigate(returnUrl && returnUrl.startsWith("/driver") ? returnUrl : "/driver/trips", { replace: true });
      } else {
        navigate(returnUrl && !returnUrl.startsWith("/admin") && !returnUrl.startsWith("/driver") ? returnUrl : "/", { replace: true });
      }
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đăng nhập thất bại. Vui lòng thử lại.";
      showToast?.(msg, "error");
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý đăng ký tài khoản mới
  const handleRegister = async (payload: RegisterRequestDto) => {
    setSubmitting(true);
    try {
      await authService.register(payload);
      showToast?.("Đăng ký tài khoản FutaBus thành công! Vui lòng đăng nhập.", "success");
      navigate("/login");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.";
      showToast?.(msg, "error");
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý đăng xuất
  const handleLogout = async () => {
    try {
      await logoutClient();
      showToast?.("Bạn đã đăng xuất khỏi hệ thống.", "info");
      navigate("/login");
    } catch {
      navigate("/login");
    }
  };

  // Xử lý gửi yêu cầu quên mật khẩu
  const handleForgotPassword = async (payload: ForgotPasswordRequestDto) => {
    setSubmitting(true);
    try {
      const msg = await authService.forgotPassword(payload);
      showToast?.(msg || "Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.", "success");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Không thể gửi yêu cầu đặt lại mật khẩu.";
      showToast?.(msg, "error");
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý xác nhận đặt lại mật khẩu mới
  const handleResetPassword = async (payload: ResetPasswordRequestDto) => {
    setSubmitting(true);
    try {
      const msg = await authService.resetPassword(payload);
      showToast?.(msg || "Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.", "success");
      navigate("/login");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đặt lại mật khẩu thất bại. Mã xác nhận có thể đã hết hạn.";
      showToast?.(msg, "error");
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  return {
    submitting,
    handleLogin,
    handleRegister,
    handleLogout,
    handleForgotPassword,
    handleResetPassword,
  };
}
