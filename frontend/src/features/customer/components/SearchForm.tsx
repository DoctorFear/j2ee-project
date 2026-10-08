// Biểu mẫu tìm chuyến
import {
  ArrowRight,
  ArrowRightLeft,
  CalendarCheck,
  CalendarDays,
  Search,
} from "lucide-react";
import StationPicker from "../../../components/StationPicker";
import { localDate } from "../../../data/customer";
import type { SearchCriteria } from "../types/search";
type Props = {
  form: SearchCriteria;
  setForm: (form: SearchCriteria) => void;
  roundTrip: boolean;
  setRoundTrip: (value: boolean) => void;
  returnDate: string;
  setReturnDate: (value: string) => void;
  onSearch: () => void;
  error: string;
};
// Đổi chiều và chọn ngày tìm kiếm mà không thay đổi bộ lọc
export default function SearchForm({
  form,
  setForm,
  roundTrip,
  setRoundTrip,
  returnDate,
  setReturnDate,
  onSearch,
  error,
}: Props) {
  return (
    <div className="search-wrapper">
      <div className="search-card">
        <div className="trip-type-row">
          <div className="radio-pill-group">
            {[false, true].map((value) => (
              <button
                key={String(value)}
                className={`radio-pill ${roundTrip === value ? "active" : ""}`}
                onClick={() => setRoundTrip(value)}
              >
                {value ? (
                  <ArrowRightLeft size={15} />
                ) : (
                  <ArrowRight size={15} />
                )}{" "}
                {value ? "Khứ hồi" : "Một chiều"}
              </button>
            ))}
          </div>
        </div>
        <div className="search-grid">
          <StationPicker
            label="Điểm đi"
            value={form.originId}
            onChange={(originId) => setForm({ ...form, originId })}
          >
            <button
              className="swap-btn"
              aria-label="Đổi chiều đi đến"
              title="Đổi chiều đi/đến"
              onClick={() =>
                setForm({
                  ...form,
                  originId: form.destinationId,
                  destinationId: form.originId,
                })
              }
            >
              <ArrowRightLeft size={16} />
            </button>
          </StationPicker>
          <StationPicker
            label="Điểm đến"
            value={form.destinationId}
            onChange={(destinationId) => setForm({ ...form, destinationId })}
          />
          <div className="input-box">
            <label htmlFor="departure-date">
              <CalendarDays size={13} /> Ngày đi
            </label>
            <input
              id="departure-date"
              type="date"
              min={localDate()}
              value={form.date}
              onChange={(e) => setForm({ ...form, date: e.target.value })}
            />
          </div>
          <div className={`input-box ${roundTrip ? "" : "disabled"}`}>
            <label htmlFor="return-date">
              <CalendarCheck size={13} /> Ngày về
            </label>
            <input
              id="return-date"
              type="date"
              min={form.date}
              disabled={!roundTrip}
              value={returnDate}
              onChange={(e) => setReturnDate(e.target.value)}
            />
          </div>
        </div>
        <div className="btn-search-row">
          <button className="btn-search" onClick={onSearch}>
            <Search size={17} />
            Tìm chuyến xe
          </button>
        </div>
        {error && (
          <p role="alert" className="form-error">
            {error}
          </p>
        )}
      </div>
    </div>
  );
}
