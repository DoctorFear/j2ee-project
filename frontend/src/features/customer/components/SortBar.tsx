// Thanh sắp xếp kết quả tìm chuyến
import { Armchair, Clock, Tags } from "lucide-react";
import type { SearchCriteria } from "../types/search";
// Chuyển thứ tự hiển thị chuyến theo giá, giờ và số ghế
export default function SortBar({
  value,
  onChange,
}: {
  value: SearchCriteria["sort"];
  onChange: (value: SearchCriteria["sort"]) => void;
}) {
  return (
    <div className="sort-row">
      {(
        [
          { key: "price", label: "Giá rẻ nhất", Icon: Tags },
          { key: "time", label: "Giờ khởi hành", Icon: Clock },
          { key: "seats", label: "Ghế trống nhiều", Icon: Armchair },
        ] as const
      ).map(({ key, label, Icon }) => (
        <button
          key={key}
          className={`sort-chip ${value === key ? "active" : ""}`}
          onClick={() => onChange(key)}
        >
          <Icon size={14} />
          {label}
        </button>
      ))}
    </div>
  );
}
