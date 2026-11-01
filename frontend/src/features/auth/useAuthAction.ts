// Hook điều phối toàn bộ nghiệp vụ Xác thực (Auth) cho giao diện FutaBus
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "./useUser";
import { sessionApi } from "./session-api";
import { authFormsApi } from "./api";
import type { ForgotPasswordRequestDto, RegisterRequestDto, ResetPasswordRequestDto } from "@/features/auth/types";
import type { LoginRequestDto } from "./types";

interface ToastApi {
  success: (msg: string) => void;
  error: (msg: string) => void;
  info: (msg: string) => void;
  warning?: (msg: string) => void;
}

export function useAuthAction(toast?: ToastApi) {
  const { loginSuccess, logoutClient } = useUser();
  const navigate = useNavigate();
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Xử lý đăng nhập
  const handleLogin = async (payload: LoginRequestDto, rememberMe = true) => {
    setSubmitting(true);
    try {
      const res = await sessionApi.login(payload);
      loginSuccess(res, rememberMe);
      toast?.success("Đăng nhập thành công! Chào mừng quý khách.");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đăng nhập thất bại. Vui lòng thử lại.";
      toast?.error(msg);
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý đăng ký tài khoản mới
  const handleRegister = async (payload: RegisterRequestDto) => {
    setSubmitting(true);
    try {
      await authFormsApi.register(payload);
      toast?.success("Đăng ký tài khoản FutaBus thành công! Vui lòng đăng nhập.");
      navigate("/login");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.";
      toast?.error(msg);
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý đăng xuất
  const handleLogout = async () => {
    try {
      await logoutClient();
      toast?.info("Bạn đã đăng xuất khỏi hệ thống.");
      navigate("/login");
    } catch {
      navigate("/login");
    }
  };

  // Xử lý gửi yêu cầu quên mật khẩu
  const handleForgotPassword = async (payload: ForgotPasswordRequestDto) => {
    setSubmitting(true);
    try {
      await authFormsApi.forgotPassword(payload);
      toast?.success("Liên kết đặt lại mật khẩu đã được gửi đến email của bạn.");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Không thể gửi yêu cầu đặt lại mật khẩu.";
      toast?.error(msg);
      return false;
    } finally {
      setSubmitting(false);
    }
  };

  // Xử lý xác nhận đặt lại mật khẩu mới
  const handleResetPassword = async (payload: ResetPasswordRequestDto) => {
    setSubmitting(true);
    try {
      await authFormsApi.resetPassword(payload);
      toast?.success("Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.");
      navigate("/login");
      return true;
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : "Đặt lại mật khẩu thất bại. Mã xác nhận có thể đã hết hạn.";
      toast?.error(msg);
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