// Hộp thoại thanh toán MoMo và kết quả giao dịch
import { Link } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";
import { Check, CircleCheck, Clock, QrCode, X } from "lucide-react";
import Modal from "../../../components/Modal";
import type { Contact } from "../../../types/contact";
import { money } from "../../../utils/customer";
type Props = {
  remaining: number;
  total: number;
  code: string;
  seats: string[];
  contact: Contact;
  routeName: string;
  success: boolean;
  onSuccess: () => void;
  onClose: () => void;
};
// Hiển thị thông tin thanh toán và kết quả giao dịch
export default function MomoPaymentModal({
  remaining,
  total,
  code,
  seats,
  contact,
  routeName,
  success,
  onSuccess,
  onClose,
}: Props) {
  return (
    <Modal
      title="Thanh toán Ví MoMo"
      className="momo-modal"
      onClose={onClose}
      header={
        <div className="momo-modal-header">
          <h3>
            <QrCode size={16} />
            Thanh toán Ví MoMo
          </h3>
          <div className="timer-badge">
            <Clock size={13} />
            <span>
              {String(Math.floor(remaining / 60)).padStart(2, "0")}:
              {String(remaining % 60).padStart(2, "0")}
            </span>
          </div>
          <button className="payment-close" aria-label="Đóng" onClick={onClose}>
            <X size={16} />
          </button>
        </div>
      }
    >
      <div className="momo-modal-body">
        {success ? (
          <div className="success-card" style={{ display: "block" }}>
            <div className="success-icon">
              <Check size={32} />
            </div>
            <h3>Thanh toán thành công!</h3>
            <p>Kết quả giả lập, chưa phát hành vé thật.</p>
            <div className="ticket-pass">
              <div className="ticket-pass-title">VÉ XE ĐIỆN TỬ FUTA — MẪU</div>
              <div>
                <b>Mã vé:</b> {code}
              </div>
              <div>
                <b>Tuyến:</b> {routeName}
              </div>
              <div>
                <b>Ghế đã đặt:</b>{" "}
                <span className="seat-result">{seats.join(", ")}</span>
              </div>
              <div>
                <b>Khách hàng:</b> {contact.name}
              </div>
            </div>
            <Link className="btn-simulate-success finish-payment" to="/">
              Hoàn tất &amp; Quay lại trang chủ
            </Link>
          </div>
        ) : (
          <>
            <p className="payment-instructions">
              Phiên thanh toán minh họa kéo dài <b>10 phút</b>. Mã QR bên dưới
              chỉ phục vụ xem trước, không chuyển tiền.
            </p>
            <div className="qr-container">
              <QRCodeSVG
                value={`FUTA-PREVIEW:${code}:${total}`}
                size={180}
                level="H"
                className="qr-img"
              />
              <div className="qr-momo-center">M</div>
            </div>
            <div className="transfer-info-list">
              <div className="transfer-row">
                <span className="t-label">Số tài khoản MoMo:</span>
                <span className="t-val">Chưa kết nối</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Người thụ hưởng:</span>
                <span className="t-val">FUTA BUS LINES CO.</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Số tiền:</span>
                <span className="t-val price">{money(total)}</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Nội dung chuyển khoản:</span>
                <span className="t-val transfer-code">{code}</span>
              </div>
            </div>
            <div className="modal-actions">
              <button className="btn-simulate-success" onClick={onSuccess}>
                <CircleCheck size={16} />
                Giả lập: Đã nhận tiền thành công
              </button>
              <button className="btn-cancel-pay" onClick={onClose}>
                Hủy thanh toán &amp; Nhả ghế
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
