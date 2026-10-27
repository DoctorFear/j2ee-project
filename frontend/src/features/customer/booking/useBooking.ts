// Quản lý lựa chọn ghế và phiên thanh toán
import { useEffect, useState } from "react";
import type { Trip } from "@/features/customer/types/trip";
import type { CustomerJourney } from "@/features/customer/types/journey";
import type {
  BookingOrder,
  PaymentSession,
} from "@/features/customer/types/order";
import { bookingApi } from "@/features/customer/api/bookingApi";
import { useUser } from "@/features/auth";
import { atStop, segment } from "@/features/customer/model/trip";
import { fare } from "@/features/customer/model/pricing";

// Kiểm tra thông tin đặt vé và giới hạn năm ghế mỗi đơn
export default function useBooking(
  trip: Trip,
  initialOrigin: string,
  initialDestination: string,
) {
  const { user } = useUser();
  const [origin, setOrigin] = useState(initialOrigin);
  const [destination, setDestination] = useState(initialDestination);
  const [selected, setSelected] = useState<string[]>([]);
  const [contact, setContact] = useState({
    name: user?.fullName || "",
    phone: user?.phoneNumber || "",
    email: user?.email || "",
  });
  const [error, setError] = useState("");
  const [transitSelected, setTransitSelected] = useState(false);
  const [message, setMessage] = useState("");
  const [order, setOrder] = useState<BookingOrder | null>(null);
  const [payment, setPayment] = useState<PaymentSession | null>(null);
  const [remaining, setRemaining] = useState(600);
  const [busy, setBusy] = useState(false);
  const key = `futa-booking:${trip.code}`;
  const success = order?.status === "PAID";
  const expiresAt = order ? new Date(order.expiresAt).getTime() : null;


  // Khôi phục phiên chưa kết thúc khi tải lại trang
  useEffect(() => {
    const id = sessionStorage.getItem(key);
    let active = true;
    if (id)
      bookingApi
        .order(id)
        .then((value) => {
          if (
            active &&
            ["HELD", "PAID", "PAYMENT_REVIEW"].includes(value.status)
          ) {
            setOrder(value);
            setSelected(value.seatCodes);
            setOrigin(value.originId);
            setDestination(value.destinationId);
            setContact(value.contact);
            if (value.status === "HELD")
              bookingApi
                .pay(value.id)
                .then((session) => {
                  if (active) setPayment(session);
                })
                .catch((error) => {
                  if (active) setError(error.message);
                });
          } else sessionStorage.removeItem(key);
        })
        .catch(() => sessionStorage.removeItem(key));
    return () => {
      active = false;
    };
  }, [key]);

  // Đếm ngược theo thời điểm hết hạn và xóa ghế khi phiên kết thúc
  useEffect(() => {
    if (!order || order.status !== "HELD") return;
    let active = true,
      serverOffset = new Date(order.serverTime).getTime() - Date.now();
    const tick = () =>
      setRemaining(
        Math.max(
          0,
          Math.ceil(
            (new Date(order.expiresAt).getTime() - Date.now() - serverOffset) /
              1000,
          ),
        ),
      );
    const poll = async () => {
      try {
        const current = await bookingApi.order(order.id);
        if (active) {
          setOrder(current);
          serverOffset = new Date(current.serverTime).getTime() - Date.now();
          if (["EXPIRED", "CANCELLED"].includes(current.status)) {
            setMessage("Phiên giữ chỗ đã kết thúc. Vui lòng chọn ghế lại.");
            setOrder(null);
            setPayment(null);
            setSelected([]);
            sessionStorage.removeItem(key);
          }
        }
      } catch (error) {
        if (active)
          setError(
            error instanceof Error
              ? error.message
              : "Không kiểm tra được thanh toán.",
          );
      }
    };
    tick();
    const timer = setInterval(tick, 1000);
    const status = setInterval(poll, 3000);
    return () => {
      active = false;
      clearInterval(timer);
      clearInterval(status);
    };
  }, [order?.id, order?.status, key]);

  const part = segment(trip, origin, destination);
  const price = order?.fare ?? fare(trip, origin, destination);
  const valid =
    contact.name.trim().length >= 2 &&
    /^0\d{9}$/.test(contact.phone) &&
    /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact.email);
  const available =
    (trip as CustomerJourney).bookable &&
    new Date(atStop(trip, origin)) > new Date();

  // Bỏ chọn hoặc thêm ghế còn trống theo giới hạn mỗi đơn
  function toggle(seat: string) {
    if (
      order ||
      busy ||
      trip.soldSeats.includes(seat) ||
      trip.heldSeats.includes(seat)
    )
      return;
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
    if (order) return;
    const stop = trip.stops.find((s) => s.stationId === id);
    if (!stop) return;
    setOrigin(id);
    if (
      trip.stops.find((s) => s.stationId === destination)!.order <= stop.order
    ) {
      const next = trip.stops.find(
        (s) =>
          s.active &&
          (s as { allowDropOff?: boolean }).allowDropOff !== false &&
          s.order > stop.order,
      );
      if (next) setDestination(next.stationId);
    }
  }
  // Mở phiên thanh toán khi đủ thông tin đặt vé
  async function startPayment() {
    if (
      busy ||
      !selected.length ||
      !valid ||
      !part ||
      !available ||
      transitSelected
    )
      return;
    setBusy(true);
    setError("");
    try {
      const current =
        order ||
        (await bookingApi.hold(
          trip.code,
          origin,
          destination,
          selected,
          contact,
        ));
      setOrder(current);
      sessionStorage.setItem(key, current.id);
      const session = await bookingApi.pay(current.id);
      setPayment(session);
      setMessage(
        "Ghế được giữ tối đa 10 phút. Vui lòng hoàn tất thanh toán MoMo.",
      );
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Không tạo được phiên thanh toán.",
      );
    } finally {
      setBusy(false);
    }
  }
  // Hủy phiên thanh toán và cập nhật trạng thái ghế
  async function cancelPayment() {
    if (!order) return;
    setBusy(true);
    try {
      if (!success) await bookingApi.release(order.id);
      sessionStorage.removeItem(key);
      setOrder(null);
      setPayment(null);
      setSelected([]);
    } catch (error) {
      setError(
        error instanceof Error
          ? error.message
          : "Không hủy được phiên thanh toán.",
      );
    } finally {
      setBusy(false);
    }
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
    expiresAt,
    paymentCode: order?.id || "",
    price,
    payment,
    order,
    busy,
    disabled:
      busy ||
      !selected.length ||
      !valid ||
      !part ||
      !available ||
      transitSelected,
    setTransitSelected,
    toggle,
    changeOrigin,
    setDestination: (id: string) => {
      if (!order) setDestination(id);
    },
    startPayment,
    cancelPayment,
  };
}
