// Nơi duy nhất hiển thị thông báo toast cho toàn ứng dụng
import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { CircleAlert, CircleCheck, Info, TriangleAlert, X } from "lucide-react";
import { ToastContext, type ToastApi, type ToastOptions, type ToastType } from "./toast-context";
import "./toast.css";

interface ToastItem {
    id: string;
    type: ToastType;
    message: string;
    title: string;
    duration: number;
}

const MAX_TOASTS = 4;

const DEFAULT_TITLE: Record<ToastType, string> = {
    success: "Thành công",
    error: "Có lỗi xảy ra",
    warning: "Lưu ý",
    info: "Thông báo",
};

const DEFAULT_DURATION: Record<ToastType, number> = {
    success: 3500,
    info: 4000,
    warning: 5000,
    error: 6000,
};

const ICONS = {
    success: CircleCheck,
    error: CircleAlert,
    warning: TriangleAlert,
    info: Info,
} as const;

// Một thẻ thông báo: tự đóng, tạm dừng khi rê chuột hoặc focus
function ToastCard({ toast, onClose }: { toast: ToastItem; onClose: (id: string) => void }) {
    const [paused, setPaused] = useState(false);
    const remaining = useRef(toast.duration);
    const close = useCallback(() => onClose(toast.id), [onClose, toast.id]);

    useEffect(() => {
        if (paused) return;
        const startedAt = Date.now();
        const timer = window.setTimeout(close, remaining.current);
        return () => {
            window.clearTimeout(timer);
            remaining.current -= Date.now() - startedAt;
        };
    }, [paused, close]);

    const Icon = ICONS[toast.type];
    return (
        <div
            className={`fb-toast fb-toast-${toast.type}`}
            role={toast.type === "error" ? "alert" : "status"}
            onMouseEnter={() => setPaused(true)}
            onMouseLeave={() => setPaused(false)}
            onFocus={() => setPaused(true)}
            onBlur={() => setPaused(false)}
        >
            <div className="fb-toast-icon">
                <Icon size={18} />
            </div>
            <div className="fb-toast-body">
                <strong>{toast.title}</strong>
                <p>{toast.message}</p>
            </div>
            <button type="button" className="fb-toast-close" aria-label="Đóng thông báo" onClick={close}>
                <X size={15} />
            </button>
            <span
                className="fb-toast-bar"
                style={{
                    animationDuration: `${toast.duration}ms`,
                    animationPlayState: paused ? "paused" : "running",
                }}
            />
        </div>
    );
}

export function ToastProvider({ children }: { children: ReactNode }) {
    const [toasts, setToasts] = useState<ToastItem[]>([]);
    const seq = useRef(0);
    const region = useRef<HTMLDivElement>(null);

    const dismiss = useCallback((id: string) => {
        setToasts((prev) => prev.filter((item) => item.id !== id));
    }, []);

    const show = useCallback((message: string, type: ToastType = "info", options?: ToastOptions) => {
        setToasts((prev) => {
            // Tránh lặp cùng một thông báo (ví dụ lỗi lặp lại khi polling)
            if (prev.some((item) => item.type === type && item.message === message)) return prev;
            seq.current += 1;
            const item: ToastItem = {
                id: `toast-${seq.current}`,
                type,
                message,
                title: options?.title ?? DEFAULT_TITLE[type],
                duration: options?.duration ?? DEFAULT_DURATION[type],
            };
            return [...prev, item].slice(-MAX_TOASTS);
        });
    }, []);

    const api = useMemo<ToastApi>(
        () => ({
            show,
            dismiss,
            success: (message, options) => show(message, "success", options),
            error: (message, options) => show(message, "error", options),
            warning: (message, options) => show(message, "warning", options),
            info: (message, options) => show(message, "info", options),
        }),
        [show, dismiss],
    );

    // Đưa vùng toast lên top layer để nổi trên cả hộp thoại <dialog> đang mở
    useEffect(() => {
        const el = region.current;
        if (!el || typeof el.showPopover !== "function") return;
        try {
            if (toasts.length === 0) {
                if (el.matches(":popover-open")) el.hidePopover();
                return;
            }
            if (el.matches(":popover-open")) el.hidePopover();
            el.showPopover();
        } catch {
            // Trình duyệt không hỗ trợ popover: vẫn hiển thị bằng position: fixed
        }
    }, [toasts]);

    return (
        <ToastContext.Provider value={api}>
            {children}
            <div ref={region} className="fb-toast-region" popover="manual" aria-live="polite">
                {toasts.map((toast) => (
                    <ToastCard key={toast.id} toast={toast} onClose={dismiss} />
                ))}
            </div>
        </ToastContext.Provider>
    );
}