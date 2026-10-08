// Thẻ vé của khách hàng
import { Link } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";
import {
  ArrowRight,
  BusFront,
  CheckCheck,
  CircleCheck,
  FileText,
  QrCode,
  RotateCcw,
} from "lucide-react";
import type { Ticket } from "../../../types/ticket";
import type { Trip } from "../../../types/trip";
import { localDate } from "../../../data/customer";
import {
  atStop,
  dateTime,
  money,
  station,
  vehicle,
} from "../../../utils/customer";
// Hiển thị trạng thái vé, hành trình và các thao tác xem vé
export default function TicketCard({
  ticket,
  trip,
  onDetail,
  onQr,
}: {
  ticket: Ticket;
  trip: Trip;
  onDetail: () => void;
  onQr: () => void;
}) {
  const completed = ticket.status === "COMPLETED";
  return (
    <article className="ticket-item-card">
      <div className="ticket-card-header">
        <div className="ticket-code-wrap">
          <span>Mã vé/Code:</span>
          <span className="ticket-code">{ticket.code}</span>
        </div>
        <div className={`status-badge ${completed ? "completed" : "success"}`}>
          {completed ? <CheckCheck size={14} /> : <CircleCheck size={14} />}{" "}
          {completed ? "Đã hoàn thành" : "Mua vé thành công"}
        </div>
      </div>
      <div className="ticket-card-body">
        <div className="journey-info">
          <div className="journey-direction">
            <ArrowRight size={14} />
            Thông tin lượt đi
          </div>
          <div className="route-points">
            <span className="route-point">{station(ticket.originId).name}</span>
            <span className="route-arrow">→</span>
            <span className="route-point">
              {station(ticket.destinationId).name}
            </span>
          </div>
          <div className="ticket-meta-grid">
            <div className="meta-item">
              <span className="meta-label">Số ghế</span>
              <span className="meta-val seat">{ticket.seatCode}</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Giờ xuất bến</span>
              <span className="meta-val">
                {dateTime(atStop(trip, ticket.originId))}
              </span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Điểm lên xe</span>
              <span className="meta-val">{station(ticket.originId).name}</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Tổng tiền</span>
              <span className="meta-val ticket-total">
                {money(ticket.fare + ticket.fee)}
              </span>
            </div>
          </div>
        </div>
        <div className="ticket-qr-side">
          <button
            className="qr-button"
            disabled={completed}
            aria-label={`Xem QR vé ${ticket.code}`}
            onClick={onQr}
          >
            <QRCodeSVG
              value={ticket.qrToken}
              size={120}
              className="qr-thumbnail"
              style={
                completed ? { filter: "grayscale(1)", opacity: 0.5 } : undefined
              }
            />
          </button>
          <span className="qr-hint">
            {completed ? "Vé đã sử dụng" : "Quét khi lên xe"}
          </span>
        </div>
      </div>
      <div className="ticket-card-footer">
        <div className="car-type-tag">
          <BusFront size={15} />
          {vehicle(trip).name}
        </div>
        <div className="action-btn-group">
          <button className="btn-action highlight" onClick={onDetail}>
            <FileText size={14} />
            Xem chi tiết vé
          </button>
          {completed ? (
            <Link
              className="btn-action"
              to={`/?origin=${ticket.originId}&destination=${ticket.destinationId}&date=${localDate(1)}`}
            >
              <RotateCcw size={14} />
              Đặt lại chuyến này
            </Link>
          ) : (
            <button className="btn-action" onClick={onQr}>
              <QrCode size={14} />
              QR Lên xe
            </button>
          )}
        </div>
      </div>
    </article>
  );
}
