// Hộp thoại lịch trình các trạm dừng
import Modal from "../../../components/Modal";
import { X } from "lucide-react";
import type { Trip } from "../../../types/trip";
import { atStop, station, time } from "../../../utils/customer";
// Hiển thị mốc giờ và trạng thái hoạt động của từng trạm
export default function ScheduleModal({
  trip,
  onClose,
}: {
  trip: Trip;
  onClose: () => void;
}) {
  return (
    <Modal
      title="Lịch trình di chuyển & Trạm đón/trả"
      className="schedule-modal"
      onClose={onClose}
      header={
        <div className="schedule-modal-header">
          <div>
            <h3>Lịch trình di chuyển &amp; Trạm đón/trả</h3>
            <span>{trip.routeName}</span>
          </div>
          <button
            className="btn-close-modal"
            onClick={onClose}
            aria-label="Đóng"
          >
            <X size={20} />
          </button>
        </div>
      }
    >
      <div className="schedule-modal-body">
        <div className="timeline-stepper">
          {trip.stops.map((stop) => (
            <div
              className={`step-item ${stop.active ? "" : "inactive"}`}
              key={stop.stationId}
            >
              <div className="step-time">
                {time(atStop(trip, stop.stationId))}
              </div>
              <div className="step-dot" />
              <div className="step-station-title">
                {station(stop.stationId).name}
                {!stop.active && (
                  <span className="step-badge-inactive">Ngưng hoạt động</span>
                )}
              </div>
              <div className="step-address">
                {station(stop.stationId).address}
              </div>
            </div>
          ))}
        </div>
      </div>
    </Modal>
  );
}
