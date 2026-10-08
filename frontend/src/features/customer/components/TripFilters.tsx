// Bộ lọc chuyến xe dạng nút lựa chọn
import { Trash2 } from "lucide-react";
import type { SearchCriteria } from "../types/search";
import { vehicleTypes } from "../../../data/customer";
type Props = {
  criteria: SearchCriteria;
  update: <K extends keyof SearchCriteria>(
    key: K,
    value: SearchCriteria[K],
  ) => void;
  onClear: () => void;
  counts: number[];
};
// Áp dụng bộ lọc giờ, xe, vị trí ghế và khoảng giá
export default function TripFilters({
  criteria,
  update,
  onClear,
  counts,
}: Props) {
  return (
    <aside className="filter-card">
      <div className="filter-head">
        <h3>BỘ LỌC TÌM KIẾM</h3>
        <button className="btn-clear" onClick={onClear}>
          <Trash2 size={13} /> Xóa lọc
        </button>
      </div>
      <div className="filter-group">
        <div className="filter-title">Giờ khởi hành</div>
        <div className="time-checkboxes">
          {[
            "Sáng sớm (00:00 - 06:00)",
            "Buổi sáng (06:00 - 12:00)",
            "Buổi chiều (12:00 - 18:00)",
            "Buổi tối (18:00 - 24:00)",
          ].map((label, i) => (
            <label key={label}>
              <span className="tc-left">
                <input
                  type="checkbox"
                  checked={criteria.timeRanges.includes(i)}
                  onChange={() =>
                    update(
                      "timeRanges",
                      criteria.timeRanges.includes(i)
                        ? criteria.timeRanges.filter((x) => x !== i)
                        : [...criteria.timeRanges, i],
                    )
                  }
                />
                {label}
              </span>
              <span className="tc-count">{counts[i]}</span>
            </label>
          ))}
        </div>
      </div>
      <div className="filter-group">
        <div className="filter-title">Loại xe</div>
        <div className="pill-filters">
          {vehicleTypes.map((v) => (
            <button
              className={`pill-btn ${criteria.vehicleTypes.includes(v.id) ? "active" : ""}`}
              aria-pressed={criteria.vehicleTypes.includes(v.id)}
              key={v.id}
              onClick={() =>
                update(
                  "vehicleTypes",
                  criteria.vehicleTypes.includes(v.id)
                    ? criteria.vehicleTypes.filter((x) => x !== v.id)
                    : [...criteria.vehicleTypes, v.id],
                )
              }
            >
              {v.id === "BUS-34" ? "Giường nằm" : "Limousine"}
            </button>
          ))}
        </div>
      </div>
      <div className="filter-group">
        <div className="filter-title">Vị trí hàng ghế</div>
        <div className="pill-filters">
          {[
            ["front", "Hàng đầu"],
            ["middle", "Hàng giữa"],
            ["rear", "Hàng cuối"],
          ].map(([value, label]) => (
            <button
              key={value}
              className={`pill-btn ${criteria.position === value ? "active" : ""}`}
              aria-pressed={criteria.position === value}
              onClick={() =>
                update("position", criteria.position === value ? "" : value)
              }
            >
              {label}
            </button>
          ))}
        </div>
      </div>
      <div className="filter-group">
        <div className="filter-title">Tầng</div>
        <div className="pill-filters">
          {[
            ["B", "Tầng trên"],
            ["A", "Tầng dưới"],
          ].map(([value, label]) => (
            <button
              key={value}
              className={`pill-btn ${criteria.deck === value ? "active" : ""}`}
              aria-pressed={criteria.deck === value}
              onClick={() =>
                update("deck", criteria.deck === value ? "" : value)
              }
            >
              {label}
            </button>
          ))}
        </div>
      </div>
      <div className="filter-group">
        <div className="filter-title">Khoảng giá vé</div>
        <div className="price-filter">
          <label>
            Giá từ (đ)
            <input
              type="number"
              min="0"
              value={criteria.minPrice}
              onChange={(e) => update("minPrice", e.target.value)}
            />
          </label>
          <label>
            Đến (đ)
            <input
              type="number"
              min="0"
              value={criteria.maxPrice}
              onChange={(e) => update("maxPrice", e.target.value)}
            />
          </label>
        </div>
      </div>
    </aside>
  );
}
