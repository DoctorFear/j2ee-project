// Trang nhận kết quả thanh toán và đối chiếu trạng thái đơn
import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { bookingApi } from "../../api/bookingApi";
import type { BookingOrder } from "./types/order";
import CustomerHeader from "../../layouts/CustomerHeader";
// Chỉ xác nhận thành công từ trạng thái đơn trên máy chủ
export default function PaymentResultPage() {
  const [params] = useSearchParams();
  const id = params.get("bookingId");
  const [order, setOrder] = useState<BookingOrder | null>(null);
  const [error, setError] = useState("");
  useEffect(() => {
    if (!id) {
      setError("Thiếu mã đơn đặt vé.");
      return;
    }
    let active = true;
    const load = () =>
      bookingApi
        .order(id)
        .then((value) => {
          if (active) {
            setOrder(value);
            setError("");
          }
        })
        .catch((error) => {
          if (active) setError(error.message);
        });
    load();
    const timer = setInterval(load, 3000);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [id]);
  return (
    <div className="booking-page">
      <CustomerHeader booking />
      <main className="fallback">
        <h1>
          {order?.status === "PAID"
            ? "Thanh toán thành công"
            : order?.status === "PAYMENT_REVIEW"
              ? "Giao dịch cần đối soát"
              : order?.status === "EXPIRED"
                ? "Phiên giữ chỗ đã hết hạn"
                : order?.status === "CANCELLED"
                  ? "Phiên thanh toán đã hủy"
                  : "Đang kiểm tra thanh toán"}
        </h1>
        <p role="status">
          {error ||
            (order?.status === "PAYMENT_REVIEW"
              ? "Tiền được ghi nhận sau khi phiên giữ ghế kết thúc. Vui lòng liên hệ hỗ trợ để đối soát; chưa phát hành vé."
              : order?.status === "PAID"
                ? "Vé đã được phát hành. Email xác nhận sẽ được gửi tới địa chỉ bạn đã đăng ký."
                : ["EXPIRED", "CANCELLED"].includes(order?.status || "")
                  ? "Ghế đã được giải phóng. Bạn có thể tìm chuyến và đặt vé lại."
                  : "Kết quả sẽ tự cập nhật sau khi MoMo thông báo tới hệ thống.")}
        </p>
        <Link to="/my-tickets">Xem vé của tôi</Link>
      </main>
    </div>
  );
}
