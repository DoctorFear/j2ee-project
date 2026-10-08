// Trang tìm kiếm chuyến xe khách hàng
import { Frown } from "lucide-react";
import CustomerHeader from "../../layouts/CustomerHeader";
import useTripSearch from "../../hooks/useTripSearch";
import SearchForm from "./components/SearchForm";
import TripFilters from "./components/TripFilters";
import TripCard from "./components/TripCard";
import SortBar from "./components/SortBar";
import ScheduleModal from "./components/ScheduleModal";
import PolicyModal from "./components/PolicyModal";
import { station, atStop, time } from "../../utils/customer";
import "./search.css";

// Ghép các thành phần tìm kiếm và hộp thoại
export default function SearchTripPage() {
  const model = useTripSearch();
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
      <CustomerHeader />
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
      />
      <div className="content-wrapper">
        <TripFilters
          criteria={criteria}
          update={model.update}
          onClear={model.clearFilters}
          counts={counts}
        />
        <main>
          <div className="results-head">
            <div>
              <h2 className="route-name">
                {station(origin).province} ({station(origin).name}) đi{" "}
                {station(destination).province}
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
          {!results.length && (
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
