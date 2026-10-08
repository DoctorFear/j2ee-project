// Hộp chọn bến xe phân nhóm theo tỉnh và tìm kiếm nhanh
import { useEffect, useRef, useState, type ReactNode } from "react";
import {
  Building2,
  Check,
  ChevronDown,
  LocateFixed,
  MapPin,
} from "lucide-react";
import { stations } from "../data/customer";
import { normalize } from "../utils/customer";

// Mở danh sách bến xe và giữ lựa chọn theo mã trạm
export default function StationPicker({
  label,
  value,
  onChange,
  children,
}: {
  label: string;
  value: string;
  onChange: (id: string) => void;
  children?: ReactNode;
}) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState("");
  const ref = useRef<HTMLDivElement>(null);
  const searchRef = useRef<HTMLInputElement>(null);
  const selected = stations.find((s) => s.id === value);
  const visible = stations.filter((s) =>
    normalize(`${s.name} ${s.province}`).includes(normalize(query)),
  );
  // Đóng danh sách khi bấm ngoài và đặt con trỏ vào ô tìm kiếm
  useEffect(() => {
    function close(e: PointerEvent) {
      if (!ref.current?.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("pointerdown", close);
    return () => document.removeEventListener("pointerdown", close);
  }, []);
  useEffect(() => {
    if (open) searchRef.current?.focus();
  }, [open]);
  return (
    <div
      className="input-box station-picker"
      ref={ref}
      onKeyDown={(e) => {
        if (e.key === "Escape") setOpen(false);
      }}
    >
      <label>
        {label === "Điểm đi" ? <MapPin size={13} /> : <LocateFixed size={13} />}{" "}
        {label}
      </label>
      <button
        className="location-display"
        aria-label={`Chọn ${label.toLowerCase()}`}
        aria-expanded={open}
        onClick={() => {
          setOpen(!open);
          setQuery("");
        }}
      >
        <span>
          {selected ? `${selected.province} (${selected.name})` : "Chọn bến xe"}
        </span>
        <ChevronDown size={12} />
      </button>
      {children}
      <div className={`station-modal ${open ? "active" : ""}`}>
        <div className="modal-search-box">
          <input
            ref={searchRef}
            className="modal-search-input"
            aria-label={`Tìm ${label.toLowerCase()}`}
            placeholder="Tìm tỉnh hoặc bến xe..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
        <div className="modal-content-list">
          {[...new Set(visible.map((s) => s.province))].map((province) => (
            <div className="province-group" key={province}>
              <div className="province-header">
                <Building2 size={14} />
                {province}
              </div>
              {visible
                .filter((s) => s.province === province)
                .map((s) => (
                  <button
                    key={s.id}
                    className="station-item"
                    aria-pressed={s.id === value}
                    onClick={() => {
                      onChange(s.id);
                      setOpen(false);
                    }}
                  >
                    <span>{s.name}</span>
                    {s.id === value && <Check size={16} />}
                  </button>
                ))}
            </div>
          ))}
          {!visible.length && (
            <div className="station-no-results">
              Không tìm thấy bến xe phù hợp
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
