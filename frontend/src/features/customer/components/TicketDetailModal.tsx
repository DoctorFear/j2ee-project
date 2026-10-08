// Hộp thoại thông tin vé điện tử
import {
  Banknote,
  BusFront,
  Info,
  LocateFixed,
  MapPin,
  Receipt,
  TriangleAlert,
  UserRound,
  X,
} from "lucide-react";
import Modal from "../../../components/Modal";
import type { Ticket } from "../../../types/ticket";
import type { Trip } from "../../../types/trip";
import { atStop, dateTime, money, station } from "../../../utils/customer";
// Hiển thị hành khách, điểm đón trả và các khoản thanh toán
export default function TicketDetailModal({
  ticket,
  trip,
  onClose,
}: {
  ticket: Ticket;
  trip: Trip;
  onClose: () => void;
}) {
  const pickupTime = dateTime(
    new Date(
      new Date(atStop(trip, ticket.originId)).getTime() - 900000,
    ).toISOString(),
  );
  return (
    <Modal
      title="Thông tin chi tiết vé"
      className="ticket-detail-modal"
      onClose={onClose}
      header={
        <div className="modal-top">
          <h3>
            <Info size={19} />
            Thông tin chi tiết vé
          </h3>
          <button
            className="btn-close-modal"
            aria-label="Đóng"
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </div>
      }
    >
      <div className="modal-body">
        <div className="trip-banner-head">
          <div>
            <span className="ticket-code-label">MÃ VÉ</span>
            <span className="tb-code">{ticket.code}</span>
          </div>
          <span className="tb-status">
            {ticket.status === "COMPLETED"
              ? "Đã hoàn thành"
              : "Mua vé thành công"}
          </span>
        </div>
        <div className="detail-card-block">
          <div className="block-header-title">
            <UserRound size={17} />
            Thông tin hành khách
          </div>
          <div className="info-grid-2">
            <div className="info-cell">
              <span className="c-label">Họ &amp; tên</span>
              <span className="c-val">{ticket.passenger.name}</span>
            </div>
            <div className="info-cell">
              <span className="c-label">Số điện thoại</span>
              <span className="c-val">{ticket.passenger.phone}</span>
            </div>
            <div className="info-cell full-width">
              <span className="c-label">Email</span>
              <span className="c-val">{ticket.passenger.email}</span>
            </div>
          </div>
          <div className="spam-notice-box">
            <TriangleAlert size={15} />
            Quý khách vui lòng kiểm tra thêm thư rác/Spam trong trường hợp chưa
            thấy email thông tin Vé ở Hộp thư đến.
          </div>
        </div>
        <div className="detail-card-block">
          <div className="block-header-title">
            <BusFront size={17} />
            Thông tin lượt đi
          </div>
          <div className="info-grid-2">
            <div className="info-cell">
              <span className="c-label">Tuyến xe</span>
              <span className="c-val">{trip.routeName}</span>
            </div>
            <div className="info-cell">
              <span className="c-label">Thời gian khởi hành</span>
              <span className="c-val">
                {dateTime(atStop(trip, ticket.originId))}
              </span>
            </div>
            <div className="info-cell">
              <span className="c-label">Số lượng vé</span>
              <span className="c-val">1 vé</span>
            </div>
            <div className="info-cell">
              <span className="c-label">Vị trí ghế</span>
              <span className="c-val highlight">{ticket.seatCode}</span>
            </div>
          </div>
          {[true, false].map((pickup) => (
            <div className="stop-point-row" key={String(pickup)}>
              <div className="stop-point-tag">
                {pickup ? <MapPin size={15} /> : <LocateFixed size={15} />}{" "}
                {pickup ? "Điểm lên xe" : "Điểm xuống xe"}
              </div>
              <div className="stop-station-name">
                {station(pickup ? ticket.originId : ticket.destinationId).name}
              </div>
              <div className="stop-station-addr">
                {
                  station(pickup ? ticket.originId : ticket.destinationId)
                    .address
                }
              </div>
              {pickup && (
                <>
                  <div className="pickup-time">
                    Thời gian có mặt: <b>{pickupTime}</b>
                  </div>
                  <div className="stop-alert-box">
                    Quý khách vui lòng có mặt tại Bến xe/VP{" "}
                    {station(ticket.originId).name} trước <b>{pickupTime}</b> để
                    kiểm tra thông tin trước khi lên xe!
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
        <div className="detail-card-block">
          <div className="block-header-title">
            <Receipt size={17} />
            Chi tiết thanh toán
          </div>
          <div className="price-calc-row">
            <span>Giá vé</span>
            <span className="p-val">{money(ticket.fare)}</span>
          </div>
          <div className="price-calc-row">
            <span>Phí thanh toán</span>
            <span className="p-val">{money(ticket.fee)}</span>
          </div>
          <div className="price-calc-row">
            <span>Thanh toán với</span>
            <span className="p-val">
              <Banknote size={14} />
              MoMo
            </span>
          </div>
          <div className="price-calc-row">
            <b>Tổng tiền</b>
            <span className="p-val final">
              {money(ticket.fare + ticket.fee)}
            </span>
          </div>
        </div>
      </div>
    </Modal>
  );
}
