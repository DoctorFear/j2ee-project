// Hộp thoại mã QR xuất trình vé lên xe
import { QRCodeSVG } from "qrcode.react";
import Modal from "../../../components/Modal";
import type { Ticket } from "../../../types/ticket";
// Hiển thị mã QR vé ở kích thước dễ quét
export default function TicketQrModal({
  ticket,
  onClose,
}: {
  ticket: Ticket;
  onClose: () => void;
}) {
  return (
    <Modal title="QR Lên xe" onClose={onClose}>
      <div className="qr-preview">
        <QRCodeSVG value={ticket.qrToken} size={240} />
        <b>{ticket.code}</b>
        <span>
          Ghế {ticket.seatCode} · {ticket.passenger.name}
        </span>
        <small>Vé mẫu, chưa có giá trị lên xe.</small>
      </div>
    </Modal>
  );
}
