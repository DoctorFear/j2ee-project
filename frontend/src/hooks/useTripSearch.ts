// Quản lý tiêu chí và kết quả tìm chuyến khách hàng
import { useState } from "react";
import { useSearchParams } from "react-router-dom";
import { trips, localDate, stations } from "../data/customer";
import { atStop, fare, segment, time, vehicle } from "../utils/customer";
import type { SearchCriteria } from "../features/customer/types/search";
import type { Trip } from "../types/trip";
const initial: SearchCriteria = {
  originId: "AN-NHON",
  destinationId: "MIEN-TAY",
  date: localDate(),
  timeRanges: [],
  vehicleTypes: [],
  position: "",
  deck: "",
  minPrice: "",
  maxPrice: "",
  sort: "time",
};

// Kiểm tra chặng, ngày đón và áp dụng bộ lọc tìm kiếm
export default function useTripSearch() {
  const [params] = useSearchParams();
  const seed = {
    ...initial,
    originId: stations.some((s) => s.id === params.get("origin"))
      ? params.get("origin")!
      : initial.originId,
    destinationId: stations.some((s) => s.id === params.get("destination"))
      ? params.get("destination")!
      : initial.destinationId,
    date:
      params.get("date") && /^\d{4}-\d{2}-\d{2}$/.test(params.get("date")!)
        ? params.get("date")!
        : initial.date,
  };
  const [form, setForm] = useState(seed);
  const [criteria, setCriteria] = useState(seed);
  const [roundTrip, setRoundTrip] = useState(false);
  const [returnDate, setReturnDate] = useState(localDate(1));
  const [direction, setDirection] = useState<"outbound" | "return">("outbound");
  const [error, setError] = useState("");
  const [modal, setModal] = useState<{
    trip: Trip;
    kind: "schedule" | "policy";
  } | null>(null);
  const origin =
    direction === "return" ? criteria.destinationId : criteria.originId;
  const destination =
    direction === "return" ? criteria.originId : criteria.destinationId;
  const day = direction === "return" ? returnDate : criteria.date;

  // Lọc theo giờ đón ở chặng được chọn và các ghế còn trống
  const results = trips
    .filter((t) => {
      const part = segment(t, origin, destination);
      if (!part || t.saleStatus !== "OPEN") return false;
      const pickup = atStop(t, origin);
      const price = fare(t, origin, destination);
      const hour = Number(time(pickup).split(":")[0]);
      const seats = vehicle(t).seats.filter(
        (s) => !t.soldSeats.includes(s.code) && !t.heldSeats.includes(s.code),
      );
      return (
        new Date(pickup).toLocaleDateString("en-CA", {
          timeZone: "Asia/Ho_Chi_Minh",
        }) === day &&
        new Date(pickup) > new Date() &&
        (!criteria.timeRanges.length ||
          criteria.timeRanges.includes(Math.floor(hour / 6))) &&
        (!criteria.vehicleTypes.length ||
          criteria.vehicleTypes.includes(t.vehicleTypeId)) &&
        (!criteria.minPrice || price >= Number(criteria.minPrice)) &&
        (!criteria.maxPrice || price <= Number(criteria.maxPrice)) &&
        seats.some(
          (s) =>
            (!criteria.deck || s.deck === criteria.deck) &&
            (!criteria.position || s.position === criteria.position),
        )
      );
    })
    .sort((a, b) =>
      criteria.sort === "price"
        ? fare(a, origin, destination) - fare(b, origin, destination)
        : criteria.sort === "seats"
          ? vehicle(b).seats.length -
            b.soldSeats.length -
            b.heldSeats.length -
            (vehicle(a).seats.length - a.soldSeats.length - a.heldSeats.length)
          : new Date(atStop(a, origin)).getTime() -
            new Date(atStop(b, origin)).getTime(),
    );

  // Cập nhật một tiêu chí lọc và giữ các lựa chọn còn lại
  const update = <K extends keyof SearchCriteria>(
    key: K,
    value: SearchCriteria[K],
  ) => setCriteria({ ...criteria, [key]: value });

  // Kiểm tra điểm đi đến và ngày trước khi tìm chuyến
  function search() {
    if (
      !form.originId ||
      !form.destinationId ||
      form.originId === form.destinationId
    ) {
      setError("Vui lòng chọn hai bến xe khác nhau.");
      return;
    }
    if (form.date < localDate() || (roundTrip && returnDate < form.date)) {
      setError("Ngày đi không được ở quá khứ; ngày về phải từ ngày đi trở đi.");
      return;
    }
    setError("");
    setCriteria({
      ...criteria,
      originId: form.originId,
      destinationId: form.destinationId,
      date: form.date,
    });
    setDirection("outbound");
  }

  // Xóa bộ lọc nhưng giữ chặng và ngày đang tìm
  function clearFilters() {
    setCriteria({
      ...initial,
      originId: criteria.originId,
      destinationId: criteria.destinationId,
      date: criteria.date,
    });
  }
  return {
    form,
    setForm,
    criteria,
    roundTrip,
    setRoundTrip,
    returnDate,
    setReturnDate,
    direction,
    setDirection,
    error,
    modal,
    setModal,
    origin,
    destination,
    day,
    results,
    update,
    search,
    clearFilters,
  };
}
