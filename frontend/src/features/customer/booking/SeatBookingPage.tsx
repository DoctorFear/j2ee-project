// Trang đặt chỗ vé xe
import {
  Link,
  Navigate,
  useLocation,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { Armchair, Ban, Check } from "lucide-react";
import type { ReactNode } from "react";
import SeatGrid from "@/features/customer/booking/components/SeatGrid";
import Toast from "@/features/customer/components/Toast";
import ContactForm from "./components/ContactForm";
import PickupDropSection from "./components/PickupDropSection";
import PriceSummary from "./components/PriceSummary";
import MomoPaymentModal from "./components/MomoPaymentModal";
import useBooking from "@/features/customer/booking/useBooking";
import { useEffect, useState } from "react";
import { bookingApi } from "@/features/customer/api/bookingApi";
import { useUser } from "@/features/auth";
import type { CustomerJourney } from "@/features/customer/types/journey";
import PaymentResultPage from "./PaymentResultPage";
import {
  bookingReturnPath,
  readBookingSelection,
} from "@/features/customer/booking/bookingSession";
import { segment, vehicle } from "@/features/customer/model/trip";
import type { Trip } from "@/features/customer/types/trip";
import "@/features/customer/booking/booking.css";

// Kiểm tra mã chuyến và chặng trên đường dẫn trước khi đặt ghế
export default function SeatBookingPage({ header }: { header: ReactNode }) {
  const { code } = useParams();
  const [params] = useSearchParams();
  const { user, isAuthenticated, loading: authLoading } = useUser();
  const location = useLocation();
  const [trip, setTrip] = useState<CustomerJourney | null>(null);
  const [loadError, setLoadError] = useState("");
  const [loading, setLoading] = useState(true);
  // Xóa yêu cầu quay lại sau khi khôi phục đầy đủ đường dẫn đặt vé
  useEffect(() => {
    const saved = readBookingSelection();
    if (
      isAuthenticated &&
      saved &&
      saved.code === code &&
      saved.path === location.pathname + location.search
    )
      sessionStorage.removeItem("futa-booking-selection");
  }, [isAuthenticated, code, location.pathname, location.search]);
  useEffect(() => {
    if (!code || code === "payment") return;
    let active = true;
    bookingApi
      .trip(code)
      .then((value) => {
        if (active) setTrip(value);
      })
      .catch((error) => {
        if (active) setLoadError(error.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [code]);
  if (authLoading)
    return (
      <div className="booking-page">
        {header}
        <main className="fallback">Đang xác thực tài khoản...</main>
      </div>
    );
  if (!isAuthenticated) {
    sessionStorage.setItem(
      "futa-booking-selection",
      JSON.stringify({
        code,
        path: location.pathname + location.search,
        origin: params.get("origin"),
        destination: params.get("destination"),
      }),
    );
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (user?.role !== "Member") return <Navigate to="/" replace />;
  const saved = readBookingSelection();
  const resumePath = saved?.code === code ? bookingReturnPath(saved) : null;
  if (resumePath && resumePath !== location.pathname + location.search)
    return <Navigate to={resumePath} replace />;
  if (code === "payment") return <PaymentResultPage />;
  if (loading)
    return (
      <div className="booking-page">
        {header}
        <main className="fallback">Đang tải chuyến xe...</main>
      </div>
    );
  const origin =
    params.get("origin") ||
    (saved && saved.code === code ? saved.origin : "") ||
    trip?.stops.find((s) => (s as { allowPickup?: boolean }).allowPickup)
      ?.stationId ||
    "";
  const destination =
    params.get("destination") ||
    (saved && saved.code === code ? saved.destination : "") ||
    [...(trip?.stops || [])]
      .reverse()
      .find((s) => (s as { allowDropOff?: boolean }).allowDropOff)?.stationId ||
    "";
  if (!trip || !segment(trip, origin, destination))
    return (
      <div className="booking-page">
        {header}
        <main className="fallback">
          <h1>{loadError || "Không tìm thấy chuyến xe hoặc chặng hợp lệ"}</h1>
          <Link to="/">Tìm chuyến khác</Link>
        </main>
      </div>
    );
  return (
    <BookingContent
      key={`${trip.code}-${origin}-${destination}`}
      trip={trip}
      origin={origin}
      destination={destination}
      header={header}
    />
  );
}

// Ghép sơ đồ xe, điểm đón trả và thông tin thanh toán
function BookingContent({
  trip,
  origin,
  destination,
  header,
}: {
  trip: Trip;
  origin: string;
  destination: string;
  header: ReactNode;
}) {
  const model = useBooking(trip, origin, destination);
  return (
    <div className="booking-page">
      {header}
      <div className="checkout-container">
        <div className="left-content">
          <div className="section-card">
            <div className="section-title">
              <Armchair size={18} />
              Chọn ghế &amp; Sơ đồ xe {vehicle(trip).name}
            </div>
            <div className="seat-status-bar">
              <div className="status-item">
                <div className="status-sample sold">
                  <Ban size={12} />
                </div>
                <span>Đã bán / Đang giữ</span>
              </div>
              <div className="status-item">
                <div className="status-sample available" />
                <span>Còn trống</span>
              </div>
              <div className="status-item">
                <div className="status-sample selected">
                  <Check size={12} />
                </div>
                <span>Đang chọn</span>
              </div>
            </div>
            <SeatGrid
              seats={vehicle(trip).seats}
              sold={trip.soldSeats}
              held={trip.heldSeats}
              selected={model.selected}
              onToggle={model.toggle}
            />
          </div>
          <PickupDropSection
            trip={trip}
            origin={model.origin}
            destination={model.destination}
            onOrigin={model.changeOrigin}
            onDestination={model.setDestination}
            onTransferChange={model.setTransitSelected}
          />
        </div>
        <div className="right-content">
          <ContactForm contact={model.contact} onChange={model.setContact} />
          <PriceSummary
            trip={trip}
            origin={model.origin}
            selected={model.selected}
            price={model.price}
            disabled={model.disabled}
            onPay={model.startPayment}
            error={model.error}
          />
        </div>
      </div>
      {model.expiresAt && (
        <MomoPaymentModal
          remaining={model.remaining}
          total={model.price * model.selected.length}
          code={model.paymentCode}
          seats={model.selected}
          contact={model.contact}
          routeName={trip.routeName}
          success={model.success}
          payment={model.payment}
          status={model.order?.status || "HELD"}
          error={model.error}
          onRetry={model.startPayment}
          onClose={model.cancelPayment}
        />
      )}
      <Toast message={model.message} onClose={() => model.setMessage("")} />
    </div>
  );
}
