// Trang tìm kiếm chuyến xe khách hàng
import { Frown } from "lucide-react";
import type { ReactNode } from "react";
import useTripSearch from "@/features/customer/search/useTripSearch";
import SearchForm from "@/features/customer/search/components/SearchForm";
import TripFilters from "@/features/customer/search/components/TripFilters";
import TripCard from "@/features/customer/search/components/TripCard";
import SortBar from "@/features/customer/search/components/SortBar";
import ScheduleModal from "@/features/customer/search/components/ScheduleModal";
import PolicyModal from "@/features/customer/booking/components/PolicyModal";
import { atStop } from "@/features/customer/model/trip";
import { station } from "@/features/customer/demo/catalog";
import { time } from "@/lib/format";
import "@/features/customer/search/search.css";

// Ghép các thành phần tìm kiếm và hộp thoại
export default function SearchTripPage({ header }: { header: ReactNode }) {
  const model = useTripSearch();
  const { origin, destination, criteria, results, modal, setModal } = model;
  const counts = [0, 1, 2, 3].map(
    (range) => results.filter((t) => Math.floor(Number(time(atStop(t, origin)).split(":")[0]) / 6) === range).length
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
      />
      <div className="content-wrapper">
        <TripFilters criteria={criteria} update={model.update} onClear={model.clearFilters} counts={counts} />
        <main>
          <div className="results-head">
            <div>
              <h2 className="route-name">
                {station(origin).province} ({station(origin).name}) đi {station(destination).province}
              </h2>
              <div className="route-sub">
                {new Date(`${model.day}T12:00:00+07:00`).toLocaleDateString("vi-VN", {
                  weekday: "long",
                  day: "2-digit",
                  month: "2-digit",
                  year: "numeric",
                })}{" "}
                · {results.length} chuyến phù hợp
              </div>
            </div>
            <SortBar value={criteria.sort} onChange={(value) => model.update("sort", value)} />
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
      {modal?.kind === "schedule" && <ScheduleModal trip={modal.trip} onClose={() => setModal(null)} />}{" "}
      {modal?.kind === "policy" && <PolicyModal onClose={() => setModal(null)} />}
    </div>
  );
}
