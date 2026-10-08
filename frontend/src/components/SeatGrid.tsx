// Sơ đồ ghế hai tầng theo cấu hình phương tiện
import { Armchair, Layers, ShipWheel } from "lucide-react";
import type { Seat } from "../types/seat";

// Hiển thị icon ghế và phân biệt ghế trống, đã bán, đang giữ
export default function SeatGrid({
  seats,
  sold,
  held,
  selected,
  onToggle,
}: {
  seats: Seat[];
  sold: string[];
  held: string[];
  selected: string[];
  onToggle: (code: string) => void;
}) {
  return (
    <div className="bus-deck-wrapper">
      {(["A", "B"] as const).map((deck) => (
        <div className="deck-container" key={deck}>
          <div className="deck-header">
            <Layers size={14} />
            {deck === "A" ? "Tầng dưới (A)" : "Tầng trên (B)"}
          </div>
          <div
            className="driver-pos"
            style={{ visibility: deck === "A" ? "visible" : "hidden" }}
            title="Bác tài"
          >
            <ShipWheel size={17} />
          </div>
          <div className="seats-grid">
            {seats
              .filter((s) => s.deck === deck)
              .map((s) => {
                const unavailable =
                  sold.includes(s.code) || held.includes(s.code);
                return (
                  <button
                    key={s.code}
                    title={
                      held.includes(s.code)
                        ? "Ghế đang được giữ"
                        : sold.includes(s.code)
                          ? "Ghế đã bán"
                          : s.code
                    }
                    className={`seat-slot ${unavailable ? "sold" : selected.includes(s.code) ? "selected" : ""}`}
                    disabled={unavailable}
                    aria-pressed={selected.includes(s.code)}
                    aria-label={`${s.code}, ${sold.includes(s.code) ? "đã bán" : held.includes(s.code) ? "đang giữ" : selected.includes(s.code) ? "đang chọn" : "còn trống"}`}
                    onClick={() => onToggle(s.code)}
                  >
                    <Armchair size={14} />
                    <span>{s.code}</span>
                  </button>
                );
              })}
          </div>
        </div>
      ))}
    </div>
  );
}
