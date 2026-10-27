// Hộp thoại thanh toán MoMo và kết quả giao dịch
import { Link } from "react-router-dom";
import { QRCodeSVG } from "qrcode.react";
import { Check, CircleCheck, Clock, QrCode, X } from "lucide-react";
import Modal from "../../../components/Modal";
import type { Contact } from "../../../types/contact";
import type { PaymentSession } from "../types/order";
import { money } from "../../../utils/customer";
type Props = {
  remaining: number;
  total: number;
  code: string;
  seats: string[];
  contact: Contact;
  routeName: string;
  success: boolean;
  payment: PaymentSession | null;
  status: string;
  error: string;
  onRetry: () => void;
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
  payment,
  status,
  error,
  onRetry,
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
            <p>
              Vé đã được phát hành. Vui lòng kiểm tra email hoặc mở danh sách
              vé.
            </p>
            <div className="ticket-pass">
              <div className="ticket-pass-title">VÉ XE ĐIỆN TỬ FUTA</div>
              <div>
                <b>Mã đơn:</b> {code}
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
            <Link
              className="btn-simulate-success finish-payment"
              to="/my-tickets"
            >
              Hoàn tất &amp; Xem vé của tôi
            </Link>
          </div>
        ) : (
          <>
            <p className="payment-instructions">
              {status === "PAYMENT_REVIEW" ? (
                "Thanh toán cần được đối soát. Vui lòng giữ mã đơn và liên hệ hỗ trợ; không thanh toán lại."
              ) : (
                <>
                  Ghế được tạm giữ trong <b>10 phút</b>. Mở MoMo hoặc quét mã QR
                  để thanh toán.
                </>
              )}
            </p>
            {error && <p role="alert">{error}</p>}
            {payment && status === "HELD" && (
              <div className="qr-container">
                <QRCodeSVG
                  value={payment.payUrl}
                  size={180}
                  level="H"
                  className="qr-img"
                />
                <div className="qr-momo-center">M</div>
              </div>
            )}
            <div className="transfer-info-list">
              <div className="transfer-row">
                <span className="t-label">Cổng thanh toán:</span>
                <span className="t-val">MoMo</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Đơn vị bán vé:</span>
                <span className="t-val">FUTA BUS LINES CO.</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Số tiền:</span>
                <span className="t-val price">{money(total)}</span>
              </div>
              <div className="transfer-row">
                <span className="t-label">Mã đơn đặt vé:</span>
                <span className="t-val transfer-code" title={code}>
                  {code.match(/.{1,12}/g)?.map((part, index) => (
                    <span key={index}>
                      {part}
                      <wbr />
                    </span>
                  ))}
                </span>
              </div>
            </div>
            <div className="modal-actions">
              {!payment && status === "HELD" && (
                <button className="btn-simulate-success" onClick={onRetry}>
                  Thử tạo thanh toán lại
                </button>
              )}
              {payment && status === "HELD" && (
                <a
                  className="btn-simulate-success"
                  href={payment.payUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  <CircleCheck size={16} />
                  Mở MoMo để thanh toán
                </a>
              )}
              <button className="btn-cancel-pay" onClick={onClose}>
                {status === "PAYMENT_REVIEW"
                  ? "Đóng"
                  : "Hủy thanh toán & Nhả ghế"}
              </button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
