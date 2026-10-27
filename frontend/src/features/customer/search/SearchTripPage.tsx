// Trang tìm kiếm chuyến xe khách hàng
import { Frown } from "lucide-react";
import { useEffect, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { useUser } from "@/features/auth";
import {
  bookingReturnPath,
  readBookingSelection,
} from "@/features/customer/booking/bookingSession";
import useTripSearch from "@/features/customer/search/useTripSearch";
import SearchForm from "./components/SearchForm";
import TripFilters from "./components/TripFilters";
import TripCard from "./components/TripCard";
import SortBar from "./components/SortBar";
import ScheduleModal from "./components/ScheduleModal";
import PolicyModal from "@/features/customer/booking/components/PolicyModal";
import { atStop } from "@/features/customer/model/trip";
import { time } from "@/lib/format";
import "@/features/customer/search/search.css";

// Ghép các thành phần tìm kiếm và hộp thoại
export default function SearchTripPage({ header }: { header: ReactNode }) {
  const model = useTripSearch();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useUser();
  // Quay lại bước chọn ghế sau khi đăng nhập Google
  useEffect(() => {
    const path = bookingReturnPath(readBookingSelection());
    if (isAuthenticated && user?.role === "Member" && path) {
      sessionStorage.removeItem("futa-booking-selection");
      navigate(path, { replace: true });
    }
  }, [isAuthenticated, user?.role, navigate]);
  const { origin, destination, criteria, results, modal, setModal } = model;
  const counts = [0, 1, 2, 3].map(
    (range) =>
      results.filter(
        (t) =>
          Math.floor(Number(time(atStop(t, origin)).split(":")[0]) / 6) ===
          range,
      ).length,
  );
  return (
    <div className="search-page">
      {header}
      <SearchForm
        form={model.form}
        setForm={model.setForm}
        roundTrip={model.roundTrip}
        setRoundTrip={(value) => {
          model.setRoundTrip(value);
          model.setDirection("outbound");
        }}
        returnDate={model.returnDate}
        setReturnDate={model.setReturnDate}
        onSearch={model.search}
        error={model.error}
        stations={model.stations}
      />
      <div className="content-wrapper">
        <TripFilters
          criteria={criteria}
          update={model.update}
          onClear={model.clearFilters}
          counts={counts}
          vehicleTypes={model.vehicleTypes}
        />
        <main>
          <div className="results-head">
            <div>
              <h2 className="route-name">
                {model.stations.find((item) => item.id === origin)?.name ||
                  "Điểm đi"}{" "}
                đi{" "}
                {model.stations.find((item) => item.id === destination)?.name ||
                  "Điểm đến"}
              </h2>
              <div className="route-sub">
                {new Date(`${model.day}T12:00:00+07:00`).toLocaleDateString(
                  "vi-VN",
                  {
                    weekday: "long",
                    day: "2-digit",
                    month: "2-digit",
                    year: "numeric",
                  },
                )}{" "}
                · {results.length} chuyến phù hợp
              </div>
            </div>
            <SortBar
              value={criteria.sort}
              onChange={(value) => model.update("sort", value)}
            />
          </div>
          {model.roundTrip && (
            <div className="direction-tabs">
              <button
                className={`pill-btn ${model.direction === "outbound" ? "active" : ""}`}
                onClick={() => model.setDirection("outbound")}
              >
                Lượt đi
              </button>
              <button
                className={`pill-btn ${model.direction === "return" ? "active" : ""}`}
                onClick={() => model.setDirection("return")}
              >
                Lượt về
              </button>
            </div>
          )}
          <div>
            {model.loading && <p role="status">Đang tải chuyến xe...</p>}
            {results.map((trip) => (
              <TripCard
                key={trip.code}
                trip={trip}
                origin={origin}
                destination={destination}
                onSchedule={() => setModal({ trip, kind: "schedule" })}
                onPolicy={() => setModal({ trip, kind: "policy" })}
              />
            ))}
          </div>
          {!model.loading && !model.error && !results.length && (
            <div className="empty-state" style={{ display: "flex" }}>
              <Frown size={48} />
              <strong>Không tìm thấy chuyến xe phù hợp</strong>
              <p>Hãy thử bỏ bớt bộ lọc hoặc chọn một ngày khởi hành khác.</p>
            </div>
          )}
        </main>
      </div>
      {modal?.kind === "schedule" && (
        <ScheduleModal trip={modal.trip} onClose={() => setModal(null)} />
      )}{" "}
      {modal?.kind === "policy" && (
        <PolicyModal onClose={() => setModal(null)} />
      )}
    </div>
  );
}
