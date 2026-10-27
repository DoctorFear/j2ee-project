// Thẻ chuyến xe với lịch trình, giá vé và số ghế còn trống
import { Link } from "react-router-dom";
import {
  Armchair,
  BusFront,
  ChevronRight,
  Circle,
  Info,
  ShieldCheck,
  Star,
} from "lucide-react";
import type { Trip } from "@/features/customer/types/trip";
import {
  atStop,
  segment,
  station,
  vehicle,
} from "@/features/customer/model/trip";
import { fare } from "@/features/customer/model/pricing";
import { money, time } from "@/lib/format";
// Hiển thị dữ liệu chuyến và điều hướng đến đúng chặng đặt vé
export default function TripCard({
  trip,
  origin,
  destination,
  onSchedule,
  onPolicy,
}: {
  trip: Trip;
  origin: string;
  destination: string;
  onSchedule: () => void;
  onPolicy: () => void;
}) {
  const count =
    vehicle(trip).seats.length - trip.soldSeats.length - trip.heldSeats.length;
  const price = fare(trip, origin, destination);
  const part = segment(trip, origin, destination)!;
  const minutes = part.destination.offsetMinutes - part.origin.offsetMinutes;
  const rating = trip.rating;
  return (
    <article className="ticket-card">
      <div className="ticket-top">
        <div className="bus-type">
          <BusFront size={15} />
          {vehicle(trip).name}
        </div>
        <div className="rating">
          <Star size={15} fill="currentColor" />
          {rating > 0 ? rating.toFixed(1) : "Chưa có"}
        </div>
      </div>
      <div className="ticket-main">
        <div className="timeline">
          <div className="point">
            <h4>{time(atStop(trip, origin))}</h4>
            <span>
              {station(origin, trip).name} ({station(origin, trip).province})
            </span>
          </div>
          <div className="travel-info">
            <span>
              {Math.floor(minutes / 60)}h {minutes % 60}m (
              {part.destination.distanceKm - part.origin.distanceKm} Km)
            </span>
            <div className="travel-line">
              <ChevronRight className="travel-bus-icon" size={15} />
            </div>
          </div>
          <div className="point">
            <h4>{time(atStop(trip, destination))}</h4>
            <span>
              {station(destination, trip).name} (
              {station(destination, trip).province})
            </span>
          </div>
        </div>
        <div className="ticket-price">
          <div
            className={`seat-badge ${count <= 5 ? "low" : count <= 12 ? "warn" : "ok"}`}
          >
            <Circle size={6} fill="currentColor" />
            {count <= 5 ? `Sắp hết · ${count} chỗ` : `${count} chỗ trống`}
          </div>
          <div>
            {trip.pricing.mode === "DISCOUNT" && (
              <span className="fare-old">
                {money(
                  fare(
                    { ...trip, pricing: { mode: "STANDARD" } },
                    origin,
                    destination,
                  ),
                )}
              </span>
            )}
            <span className="fare">{money(price)}</span>
          </div>
        </div>
      </div>
      <div className="ticket-footer">
        <div className="quick-links">
          <button onClick={onSchedule}>
            <Info size={13} />
            Lịch trình
          </button>
          <button onClick={onPolicy}>
            <ShieldCheck size={13} />
            Chính sách
          </button>
        </div>
        <Link
          className="btn-select"
          to={`/trips/${encodeURIComponent(trip.code)}/book?origin=${encodeURIComponent(origin)}&destination=${encodeURIComponent(destination)}`}
        >
          Chọn ghế
        </Link>
      </div>
    </article>
  );
}
