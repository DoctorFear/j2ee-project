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
          Chính sách hủy và hoàn tiền sẽ được công bố khi hệ thống mở thanh toán
          chính thức.
        </p>
      </div>
    </Modal>
  );
}
