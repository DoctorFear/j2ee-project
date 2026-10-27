// Hộp thoại chính sách vé và quy định đón khách
import Modal from "../../../components/Modal";
import { ShieldCheck } from "lucide-react";
// Hiển thị quy định đón khách và chính sách vé
export default function PolicyModal({ onClose }: { onClose: () => void }) {
  return (
    <Modal title="Chính sách vé" onClose={onClose}>
      <div className="policy-content">
        <h3>
          <ShieldCheck size={18} />
          Quy định đón khách
        </h3>
        <p>
          Quý khách vui lòng có mặt trước giờ đón 15 phút và xuất trình mã QR vé
          khi lên xe.
        </p>
        <p>
          Chỉ tiếp nhận hủy vé khi còn ít nhất 5 tiếng trước giờ xe khởi hành
          tại bến đầu tuyến. Vé được hoàn toàn bộ giá vé qua MoMo; vé và ghế chỉ
          được hủy sau khi hoàn tiền thành công. Không nhận hủy sát giờ khởi
          hành.
        </p>
      </div>
    </Modal>
  );
}
