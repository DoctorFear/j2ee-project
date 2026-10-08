// Thông báo ngắn có hiệu ứng xuất hiện và tự đóng
import { useEffect } from "react";
import { CircleCheck } from "lucide-react";

// Tự ẩn thông báo sau ba giây
export default function Toast({
  message,
  onClose,
}: {
  message: string;
  onClose: () => void;
}) {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(onClose, 3000);
    return () => clearTimeout(timer);
  }, [message, onClose]);
  return (
    <div className={`toast ${message ? "show" : ""}`} role="status">
      <CircleCheck size={16} />
      <span>{message}</span>
    </div>
  );
}
