// Trang đặt chỗ vé xe
import { Link, useParams, useSearchParams } from "react-router-dom";
import { Armchair, Ban, Check } from "lucide-react";
import CustomerHeader from "../../layouts/CustomerHeader";
import SeatGrid from "../../components/SeatGrid";
import Toast from "../../components/Toast";
import ContactForm from "./components/ContactForm";
import PickupDropSection from "./components/PickupDropSection";
import PriceSummary from "./components/PriceSummary";
import MomoPaymentModal from "./components/MomoPaymentModal";
import useBooking from "../../hooks/useBooking";
import { trips } from "../../data/customer";
import { segment, vehicle } from "../../utils/customer";
import type { Trip } from "../../types/trip";
import "./booking.css";

// Kiểm tra mã chuyến và chặng trên đường dẫn trước khi đặt ghế
export default function SeatBookingPage() {
  const { code } = useParams();
  const [params] = useSearchParams();
  const trip = trips.find((t) => t.code === code);
  const origin = params.get("origin") || trip?.stops[0].stationId || "";
  const destination =
    params.get("destination") || trip?.stops.at(-1)?.stationId || "";
  if (!trip || !segment(trip, origin, destination))
    return (
      <div className="booking-page">
        <CustomerHeader booking />
        <main className="fallback">
          <h1>Không tìm thấy chuyến xe hoặc chặng hợp lệ</h1>
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
    />
  );
}

// Ghép sơ đồ xe, điểm đón trả và thông tin thanh toán
function BookingContent({
  trip,
  origin,
  destination,
}: {
  trip: Trip;
  origin: string;
  destination: string;
}) {
  const model = useBooking(trip, origin, destination);
  return (
    <div className="booking-page">
      <CustomerHeader booking />
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
          onSuccess={() => model.setSuccess(true)}
          onClose={model.cancelPayment}
        />
      )}
      <Toast message={model.message} onClose={() => model.setMessage("")} />
    </div>
  );
}
