// Component hiển thị thông báo Toast Notification hiện đại theo phong cách FutaBus
import React from "react";
import { CheckCircle2, AlertCircle, Info, X } from "lucide-react";
import type { ToastItem } from "../../hooks/useToastNotifications";

interface ToastNotificationProps {
  toasts: ToastItem[];
  onClose: (id: string) => void;
}

export const ToastNotification: React.FC<ToastNotificationProps> = ({ toasts, onClose }) => {
  if (toasts.length === 0) return null;

  return (
    <div
      style={{
        position: "fixed",
        top: "24px",
        right: "24px",
        zIndex: 9999,
        display: "flex",
        flexDirection: "column",
        gap: "10px",
        maxWidth: "420px",
        width: "calc(100vw - 48px)",
        pointerEvents: "none",
      }}
    >
      {toasts.map((toast) => {
        const isSuccess = toast.type === "success";
        const isError = toast.type === "error";

        const bg = isSuccess ? "#ecfdf5" : isError ? "#fef2f2" : "#fff2ed";
        const borderColor = isSuccess ? "#a7f3d0" : isError ? "#fecaca" : "#fed7aa";
        const textColor = isSuccess ? "#065f46" : isError ? "#991b1b" : "#9a3412";
        const iconColor = isSuccess ? "#059669" : isError ? "#dc2626" : "#f95721";

        return (
          <div
            key={toast.id}
            style={{
              pointerEvents: "auto",
              display: "flex",
              alignItems: "flex-start",
              gap: "12px",
              padding: "14px 16px",
              borderRadius: "12px",
              background: bg,
              border: `1.5px solid ${borderColor}`,
              color: textColor,
              boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04)",
              animation: "toastSlideIn 0.25s cubic-bezier(0.16, 1, 0.3, 1)",
              fontSize: "14px",
              fontWeight: 600,
              lineHeight: 1.4,
            }}
          >
            <div style={{ flexShrink: 0, marginTop: "1px", color: iconColor }}>
              {isSuccess && <CheckCircle2 size={18} />}
              {isError && <AlertCircle size={18} />}
              {!isSuccess && !isError && <Info size={18} />}
            </div>

            <div style={{ flex: 1, wordBreak: "break-word" }}>{toast.message}</div>

            <button
              onClick={() => onClose(toast.id)}
              type="button"
              style={{
                background: "transparent",
                border: "none",
                color: textColor,
                cursor: "pointer",
                padding: "2px",
                display: "flex",
                alignItems: "center",
                opacity: 0.7,
                transition: "opacity 0.15s",
              }}
              onMouseEnter={(e) => (e.currentTarget.style.opacity = "1")}
              onMouseLeave={(e) => (e.currentTarget.style.opacity = "0.7")}
              aria-label="Đóng thông báo"
            >
              <X size={16} />
            </button>
          </div>
        );
      })}
    </div>
  );
};
