// Thẻ vé của khách hàng
import { Link } from "react-router-dom";
import {
  ArrowRight,
  BusFront,
  CheckCheck,
  CircleCheck,
  FileText,
  QrCode,
  RotateCcw,
  TicketCheck,
} from "lucide-react";
import type { CustomerTicket } from "@/features/customer/api/bookingApi";
import type { Trip } from "@/features/customer/types/trip";
import { localDate } from "@/lib/localDate";
import {
  atStop,
  station,
  vehicle,
} from "@/features/customer/model/trip";
import { dateTime, money } from "@/lib/format";
// Gom các vé cùng đơn nhưng vẫn giữ thao tác riêng cho từng QR
export default function TicketCard({
  bookingId,
  tickets,
  onDetail,
  onQr,
}: {
  bookingId: string;
  tickets: CustomerTicket[];
  onDetail: (ticket: CustomerTicket) => void;
  onQr: (ticket: CustomerTicket) => void;
}) {
  const ticket = tickets[0];
  const trip: Trip = ticket.trip;
  const completed = ticket.status !== "PAID";
  const total = tickets.reduce((sum, item) => sum + item.fare + item.fee, 0);
  const seatCodes = tickets.map((item) => item.seatCode).join(", ");
  return (
    <article className="ticket-item-card">
      <div className="ticket-card-header">
        <div className="ticket-code-wrap">
          <span>Mã đơn đặt vé:</span>
          <span className="ticket-code">{bookingId}</span>
        </div>
        <div className={`status-badge ${completed ? "completed" : "success"}`}>
          {completed ? <CheckCheck size={14} /> : <CircleCheck size={14} />}{" "}
          {ticket.status === "CANCELLED"
            ? "Đã hủy và hoàn tiền"
            : completed
              ? "Đã hoàn thành"
              : "Đã thanh toán"}
        </div>
      </div>
      <div className="ticket-card-body">
        <div className="journey-info">
          <div className="journey-direction">
            <ArrowRight size={14} />
            Thông tin hành trình
          </div>
          <div className="route-points">
            <span className="route-point">
              {station(ticket.originId, trip).name}
            </span>
            <span className="route-arrow">→</span>
            <span className="route-point">
              {station(ticket.destinationId, trip).name}
            </span>
          </div>
          <div className="ticket-meta-grid">
            <div className="meta-item">
              <span className="meta-label">Số lượng vé</span>
              <span className="meta-val">{tickets.length} vé</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Ghế đã đặt</span>
              <span className="meta-val seat">{seatCodes}</span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Giờ xuất bến</span>
              <span className="meta-val">
                {dateTime(atStop(trip, ticket.originId))}
              </span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Điểm lên xe</span>
              <span className="meta-val">
                {station(ticket.originId, trip).name}
              </span>
            </div>
            <div className="meta-item">
              <span className="meta-label">Tổng tiền đơn</span>
              <span className="meta-val ticket-total">
                {money(total)}
              </span>
            </div>
          </div>
        </div>
        <div className="ticket-booking-count">
          <TicketCheck size={28} />
          <b>{tickets.length} vé cùng đơn</b>
          <span>Xem QR riêng cho từng ghế phía dưới</span>
        </div>
      </div>
      <div className="booking-ticket-list">
        <div className="booking-ticket-list-title">
          <TicketCheck size={16} /> Vé trong đơn ({tickets.length})
        </div>
        {tickets.map((item) => (
          <div className="booking-ticket-row" key={item.code}>
            <div>
              <span className="ticket-code-label">Mã vé</span>
              <b>{item.code}</b>
            </div>
            <span className="booking-seat">Ghế {item.seatCode}</span>
            <div className="action-btn-group">
              <button className="btn-action highlight" onClick={() => onDetail(item)}>
                <FileText size={14} /> Chi tiết
              </button>
              {completed ? (
                <span className="ticket-row-status">
                  {item.status === "CANCELLED" ? "Đã hủy" : "Đã sử dụng"}
                </span>
              ) : (
                <button className="btn-action" onClick={() => onQr(item)}>
                  <QrCode size={14} /> QR lên xe
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
      <div className="ticket-card-footer">
        <div className="car-type-tag">
          <BusFront size={15} />
          {vehicle(trip).name}
        </div>
        <div className="action-btn-group">
          {completed ? (
            <Link
              className="btn-action"
              to={`/?origin=${ticket.originId}&destination=${ticket.destinationId}&date=${localDate(1)}`}
            >
              <RotateCcw size={14} />
              Đặt lại chuyến này
            </Link>
          ) : null}
        </div>
      </div>
    </article>
  );
}
