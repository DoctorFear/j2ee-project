// Trang đặt chỗ vé xe
import { Link, useParams, useSearchParams } from "react-router-dom";
import { Armchair, Ban, Check } from "lucide-react";
import type { ReactNode } from "react";
import SeatGrid from "@/features/customer/booking/components/SeatGrid";
import Toast from "@/features/customer/components/Toast";
import ContactForm from "@/features/customer/booking/components/ContactForm";
import PickupDropSection from "@/features/customer/booking/components/PickupDropSection";
import PriceSummary from "@/features/customer/booking/components/PriceSummary";
import MomoPaymentModal from "@/features/customer/booking/components/MomoPaymentModal";
import useBooking from "@/features/customer/booking/useBooking";
import { trips } from "@/features/customer/demo/demoTrips";
import { segment } from "@/features/customer/model/trip";
import { vehicle } from "@/features/customer/demo/catalog";
import type { Trip } from "@/features/customer/types/trip";
import "@/features/customer/booking/booking.css";

// Kiểm tra mã chuyến và chặng trên đường dẫn trước khi đặt ghế
export default function SeatBookingPage({ header }: { header: ReactNode }) {
  const { code } = useParams();
  const [params] = useSearchParams();
  const trip = trips.find((t) => t.code === code);
  const origin = params.get("origin") || trip?.stops[0].stationId || "";
  const destination = params.get("destination") || trip?.stops.at(-1)?.stationId || "";
  if (!trip || !segment(trip, origin, destination))
    return (
      <div className="booking-page">
        {header}
        <main className="fallback">
          <h1>Không tìm thấy chuyến xe hoặc chặng hợp lệ</h1>
          <Link to="/">Tìm chuyến khác</Link>
        </main>
      </div>
    );
  return (
    <BookingContent
      key={`${trip.code}-${origin}-${destination}`}
      header={header}
      trip={trip}
      origin={origin}
      destination={destination}
    />
  );
}

// Ghép sơ đồ xe, điểm đón trả và thông tin thanh toán
function BookingContent({
  header,
  trip,
  origin,
  destination,
}: {
  header: ReactNode;
  trip: Trip;
  origin: string;
  destination: string;
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
          onSuccess={() => model.setSuccess(true)}
          onClose={model.cancelPayment}
        />
      )}
      <Toast message={model.message} onClose={() => model.setMessage("")} />
    </div>
  );
}
