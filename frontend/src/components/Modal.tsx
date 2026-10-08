// Hộp thoại dùng chung có khóa cuộn và hỗ trợ bàn phím
import { useEffect, useRef, type ReactNode } from "react";
import { X } from "lucide-react";

// Mở hộp thoại ở giữa màn hình và khôi phục vị trí cuộn khi đóng
export default function Modal({
  title,
  onClose,
  children,
  className = "",
  header,
}: {
  title: string;
  onClose: () => void;
  children: ReactNode;
  className?: string;
  header?: ReactNode;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    const dialog = ref.current!;
    const previous = document.body.style.overflow;
    dialog.showModal();
    document.body.style.overflow = "hidden";
    return () => {
      dialog.close();
      document.body.style.overflow = previous;
    };
  }, []);
  return (
    <dialog
      ref={ref}
      aria-label={title}
      className={`customer-modal ${className}`}
      onCancel={onClose}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      {header || (
        <div className="modal-heading">
          <h2>{title}</h2>
          <button
            className="btn-close-modal"
            onClick={onClose}
            aria-label="Đóng"
          >
            <X size={20} />
          </button>
        </div>
      )}
      {children}
    </dialog>
  );
}
