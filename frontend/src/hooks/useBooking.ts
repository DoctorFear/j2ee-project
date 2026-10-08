// Quản lý lựa chọn ghế và phiên thanh toán
import { useEffect, useState } from "react";
import type { Trip } from "../types/trip";
import { atStop, fare, segment } from "../utils/customer";

// Kiểm tra thông tin đặt vé và giới hạn năm ghế mỗi đơn
export default function useBooking(
  trip: Trip,
  initialOrigin: string,
  initialDestination: string,
) {
  const [origin, setOrigin] = useState(initialOrigin);
  const [destination, setDestination] = useState(initialDestination);
  const [selected, setSelected] = useState<string[]>([]);
  const [contact, setContact] = useState({ name: "", phone: "", email: "" });
  const [error, setError] = useState("");
  const [transitSelected, setTransitSelected] = useState(false);
  const [message, setMessage] = useState("");
  const [expiresAt, setExpiresAt] = useState<number | null>(null);
  const [remaining, setRemaining] = useState(600);
  const [success, setSuccess] = useState(false);
  const [paymentCode, setPaymentCode] = useState("");

  // Đếm ngược theo thời điểm hết hạn và xóa ghế khi phiên kết thúc
  useEffect(() => {
    if (!expiresAt || success) return;
    function tick() {
      const seconds = Math.max(0, Math.ceil((expiresAt! - Date.now()) / 1000));
      setRemaining(seconds);
      if (!seconds) {
        setExpiresAt(null);
        setSelected([]);
        setMessage("Phiên minh họa hết hạn. Vui lòng chọn ghế lại.");
      }
    }
    tick();
    const timer = setInterval(tick, 1000);
    return () => clearInterval(timer);
  }, [expiresAt, success]);

  const part = segment(trip, origin, destination);
  const price = fare(trip, origin, destination);
  const valid =
    contact.name.trim().length >= 2 &&
    /^0\d{9}$/.test(contact.phone) &&
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact.email);
  const available =
    trip.saleStatus === "OPEN" && new Date(atStop(trip, origin)) > new Date();

  // Bỏ chọn hoặc thêm ghế còn trống theo giới hạn mỗi đơn
  function toggle(seat: string) {
    if (trip.soldSeats.includes(seat) || trip.heldSeats.includes(seat)) return;
    if (selected.includes(seat))
      setSelected(selected.filter((s) => s !== seat));
    else if (selected.length >= 5)
      setMessage("Mỗi đơn chỉ được chọn tối đa 5 ghế.");
    else {
      setSelected([...selected, seat]);
      setError("");
    }
  }

  // Điều chỉnh điểm trả khi điểm đón mới nằm phía sau
  function changeOrigin(id: string) {
    const stop = trip.stops.find((s) => s.stationId === id);
    if (!stop) return;
    setOrigin(id);
    if (
      trip.stops.find((s) => s.stationId === destination)!.order <= stop.order
    ) {
      const next = trip.stops.find((s) => s.active && s.order > stop.order);
      if (next) setDestination(next.stationId);
    }
  }

  // Mở phiên thanh toán khi đủ thông tin đặt vé
  function startPayment() {
    if (!selected.length || !valid || !part || !available || transitSelected)
      return;
    setSuccess(false);
    setRemaining(600);
    setPaymentCode(`FUTA-${Date.now().toString().slice(-6)}`);
    setExpiresAt(Date.now() + 600000);
    setMessage("Đã mở phiên thanh toán minh họa trong 10 phút.");
  }

  // Hủy phiên thanh toán và cập nhật trạng thái ghế
  function cancelPayment() {
    setExpiresAt(null);
    if (!success) setMessage("Đã hủy phiên thanh toán minh họa.");
  }

  return {
    origin,
    destination,
    selected,
    contact,
    setContact,
    error,
    message,
    setMessage,
    remaining,
    success,
    setSuccess,
    expiresAt,
    paymentCode,
    price,
    disabled:
      !selected.length || !valid || !part || !available || transitSelected,
    setTransitSelected,
    toggle,
    changeOrigin,
    setDestination,
    startPayment,
    cancelPayment,
  };
}
