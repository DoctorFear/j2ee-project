// Bảng tổng kết ghế, giá vé và phương thức thanh toán
import { CircleCheck, LockKeyhole, Wallet } from "lucide-react";
import type { Trip } from "../../../types/trip";
import { atStop, dateTime, money, vehicle } from "../../../utils/customer";
// Tổng hợp số ghế và tiền thanh toán từ chặng đang chọn
export default function PriceSummary({
  trip,
  origin,
  selected,
  price,
  disabled,
  onPay,
  error,
}: {
  trip: Trip;
  origin: string;
  selected: string[];
  price: number;
  disabled: boolean;
  onPay: () => void;
  error: string;
}) {
  const total = selected.length * price;
  return (
    <div className="summary-card">
      <div className="summary-header">
        <span>Thông tin chuyến đi</span>
        <span className="vehicle-summary">
          <CircleCheck size={14} />
          {vehicle(trip).name.includes("Limousine")
            ? "Limousine"
            : "Giường nằm"}
        </span>
      </div>
      <div className="summary-item">
        <span className="label">Tuyến xe</span>
        <span className="val">{trip.routeName}</span>
      </div>
      <div className="summary-item">
        <span className="label">Thời gian đón khách</span>
        <span className="val">{dateTime(atStop(trip, origin))}</span>
      </div>
      <div className="summary-item">
        <span className="label">Số lượng ghế</span>
        <span className="val">{selected.length} Ghế</span>
      </div>
      <div className="summary-item">
        <span className="label">Số ghế</span>
        <div className="seats-tag-list">
          {selected.length ? (
            [...selected].sort().map((s) => (
              <span className="seat-tag" key={s}>
                {s}
              </span>
            ))
          ) : (
            <span className="no-seats">Chưa chọn ghế</span>
          )}
        </div>
      </div>
      <div className="divider" />
      <div className="payment-title">
        <Wallet size={16} />
        Phương thức thanh toán
      </div>
      <div className="momo-pay-box">
        <div className="momo-logo-img">M</div>
        <div className="momo-info">
          <h4>Chuyển khoản Ví MoMo</h4>
          <p>Quét mã QR chuyển khoản tức thì 24/7</p>
        </div>
      </div>
      <div className="divider" />
      <div className="summary-item">
        <span className="label">Giá vé lượt đi</span>
        <span className="val">{money(total)}</span>
      </div>
      <div className="summary-item">
        <span className="label">Phí thanh toán</span>
        <span className="val">{money(0)}</span>
      </div>
      <div className="total-row">
        <span className="total-label">Tổng tiền</span>
        <span className="total-price">{money(total)}</span>
      </div>
      <button className="btn-checkout" disabled={disabled} onClick={onPay}>
        <LockKeyhole size={16} />
        Thanh toán ngay
      </button>
      {error && (
        <p role="alert" className="form-error">
          {error}
        </p>
      )}
    </div>
  );
}
