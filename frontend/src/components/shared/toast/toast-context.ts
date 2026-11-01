import { createContext, useContext } from "react";

export type ToastType = "success" | "error" | "warning" | "info";

export interface ToastOptions {
    /** Tiêu đề in đậm phía trên nội dung (mặc định theo loại thông báo) */
    title?: string;
    /** Thời gian tự đóng (ms) */
    duration?: number;
}

export interface ToastApi {
    show: (message: string, type?: ToastType, options?: ToastOptions) => void;
    success: (message: string, options?: ToastOptions) => void;
    error: (message: string, options?: ToastOptions) => void;
    warning: (message: string, options?: ToastOptions) => void;
    info: (message: string, options?: ToastOptions) => void;
    dismiss: (id: string) => void;
}

export const ToastContext = createContext<ToastApi | null>(null);

// Hook dùng chung cho toàn bộ ứng dụng (khách hàng + admin)
export function useToast(): ToastApi {
    const ctx = useContext(ToastContext);
    if (!ctx) throw new Error("useToast phải được dùng bên trong <ToastProvider>");
    return ctx;
}