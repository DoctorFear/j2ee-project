// Tra cứu phương tiện, bến xe và chặng của chuyến
import type { Trip } from "@/features/customer/types/trip";
import type { CustomerJourney } from "@/features/customer/types/journey";
import { stations } from "@/features/customer/demo/stations";
import { vehicleTypes } from "@/features/customer/demo/vehicleTypes";

// Lấy trạm từ hành trình thực tế hoặc dữ liệu mẫu
export const station = (id: string, trip?: Trip) =>
  (trip as CustomerJourney | undefined)?.stations?.find((item) => item.id === id) ||
  stations.find((item) => item.id === id)!;

// Lấy cấu hình xe từ hành trình thực tế hoặc dữ liệu mẫu
export const vehicle = (trip: Trip) =>
  (trip as CustomerJourney).vehicle ||
  vehicleTypes.find((item) => item.id === trip.vehicleTypeId)!;
export function segment(trip: Trip, originId: string, destinationId: string) {
  const origin = trip.stops.find((s) => s.stationId === originId);
  const destination = trip.stops.find((s) => s.stationId === destinationId);
  return origin &&
    destination &&
    origin.active &&
    destination.active &&
    (origin as { allowPickup?: boolean }).allowPickup !== false &&
    (destination as { allowDropOff?: boolean }).allowDropOff !== false &&
    origin.order < destination.order
    ? { origin, destination }
    : null;
}

// Tính giờ ghé trạm từ thời gian tích lũy
export const atStop = (trip: Trip, id: string) =>
  new Date(
    new Date(trip.departureAt).getTime() + trip.stops.find((s) => s.stationId === id)!.offsetMinutes * 60000
  ).toISOString();
