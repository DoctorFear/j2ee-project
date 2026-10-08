// Thanh chuyển đổi vé sắp khởi hành và lịch sử vé
import { Clock, History } from "lucide-react";
// Hiển thị số lượng vé ở từng tab
export default function TicketTabs({
  tab,
  onChange,
  upcoming,
  history,
}: {
  tab: "PAID" | "COMPLETED";
  onChange: (tab: "PAID" | "COMPLETED") => void;
  upcoming: number;
  history: number;
}) {
  return (
    <div className="tabs-nav">
      <button
        className={`tab-btn ${tab === "PAID" ? "active" : ""}`}
        onClick={() => onChange("PAID")}
      >
        <Clock size={16} />
        SẮP KHỞI HÀNH<span className="tab-badge">{upcoming}</span>
      </button>
      <button
        className={`tab-btn ${tab === "COMPLETED" ? "active" : ""}`}
        onClick={() => onChange("COMPLETED")}
      >
        <History size={16} />
        LỊCH SỬ VÉ<span className="tab-badge">{history}</span>
      </button>
    </div>
  );
}
