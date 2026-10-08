// Tra cứu phương tiện, bến xe và chặng của chuyến
import type { Trip } from "../types/trip";
import { stations } from "../data/stations";
import { vehicleTypes } from "../data/vehicleTypes";

// Lấy thông tin bến xe theo mã trạm
export const station = (id: string) => stations.find((s) => s.id === id)!;

// Lấy cấu hình xe của chuyến
export const vehicle = (trip: Trip) =>
  vehicleTypes.find((v) => v.id === trip.vehicleTypeId)!;

// Kiểm tra điểm trả nằm sau điểm đón đang hoạt động
export function segment(trip: Trip, originId: string, destinationId: string) {
  const origin = trip.stops.find((s) => s.stationId === originId);
  const destination = trip.stops.find((s) => s.stationId === destinationId);
  return origin &&
    destination &&
    origin.active &&
    destination.active &&
    origin.order < destination.order
    ? { origin, destination }
    : null;
}

// Tính giờ ghé trạm từ thời gian tích lũy
export const atStop = (trip: Trip, id: string) =>
  new Date(
    new Date(trip.departureAt).getTime() +
      trip.stops.find((s) => s.stationId === id)!.offsetMinutes * 60000,
  ).toISOString();
