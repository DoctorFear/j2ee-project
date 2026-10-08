// Chọn điểm đón trả và hình thức trung chuyển
import { useState } from "react";
import { Building2, LocateFixed, MapPinned, MapPin, Bus } from "lucide-react";
import type { Trip } from "../../../types/trip";
import { atStop, dateTime, station } from "../../../utils/customer";
// Giữ điểm trả sau điểm đón và hiển thị thông tin trung chuyển
export default function PickupDropSection({
  trip,
  origin,
  destination,
  onOrigin,
  onDestination,
  onTransferChange,
}: {
  trip: Trip;
  origin: string;
  destination: string;
  onOrigin: (id: string) => void;
  onDestination: (id: string) => void;
  onTransferChange: (value: boolean) => void;
}) {
  const [pickupType, setPickupType] = useState("station");
  const [dropType, setDropType] = useState("station");
  return (
    <div className="section-card">
      <div className="section-title">
        <MapPinned size={18} />
        Thông tin đón trả
      </div>
      {[true, false].map((pickup) => {
        const id = pickup ? origin : destination;
        const type = pickup ? pickupType : dropType;
        const setType = pickup ? setPickupType : setDropType;
        const candidates = trip.stops.filter(
          (s) =>
            s.active &&
            (pickup
              ? s.order < trip.stops.at(-1)!.order
              : s.order >
                trip.stops.find((s) => s.stationId === origin)!.order),
        );
        return (
          <div className="point-block" key={String(pickup)}>
            <div className="point-title">
              {pickup ? <MapPin size={16} /> : <LocateFixed size={16} />}{" "}
              {pickup ? "Điểm đón" : "Điểm trả"}
            </div>
            <div className="type-switcher">
              <button
                className={`switch-btn ${type === "station" ? "active" : ""}`}
                onClick={() => {
                  setType("station");
                  onTransferChange(
                    (pickup ? dropType : pickupType) === "transit",
                  );
                }}
              >
                <Building2 size={14} />
                Bến xe / VP
              </button>
              <button
                className={`switch-btn ${type === "transit" ? "active" : ""}`}
                onClick={() => {
                  setType("transit");
                  onTransferChange(true);
                }}
              >
                <Bus size={15} />
                Trung chuyển
              </button>
            </div>
            {type === "station" ? (
              <select
                aria-label={pickup ? "Điểm đón" : "Điểm trả"}
                className="station-select"
                value={id}
                onChange={(e) =>
                  pickup
                    ? onOrigin(e.target.value)
                    : onDestination(e.target.value)
                }
              >
                {candidates.map((s) => (
                  <option key={s.stationId} value={s.stationId}>
                    {station(s.stationId).name} ({station(s.stationId).address})
                  </option>
                ))}
              </select>
            ) : (
              <div className="transit-preview">
                <MapPin size={16} />
                <p>
                  Chưa có dữ liệu khu vực trung chuyển của chuyến này. Vui lòng
                  chọn Bến xe / VP để tiếp tục.
                </p>
              </div>
            )}
            {pickup && (
              <div className="notice-box">
                Quý khách vui lòng có mặt tại Bến xe/Văn Phòng{" "}
                <strong>{station(origin).name}</strong> trước{" "}
                <strong>
                  {dateTime(
                    new Date(
                      new Date(atStop(trip, origin)).getTime() - 900000,
                    ).toISOString(),
                  )}
                </strong>{" "}
                để kiểm tra thông tin trước khi lên xe.
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
