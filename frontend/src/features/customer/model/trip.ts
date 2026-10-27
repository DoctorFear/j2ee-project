// Tra cứu phương tiện, bến xe và chặng của chuyến
import type { Trip } from "@/features/customer/types/trip";
export function segment(trip: Trip, originId: string, destinationId: string) {
  const origin = trip.stops.find((s) => s.stationId === originId);
  const destination = trip.stops.find((s) => s.stationId === destinationId);
  return origin && destination && origin.active && destination.active && origin.order < destination.order
    ? { origin, destination }
    : null;
}

// Tính giờ ghé trạm từ thời gian tích lũy
export const atStop = (trip: Trip, id: string) =>
  new Date(
    new Date(trip.departureAt).getTime() + trip.stops.find((s) => s.stationId === id)!.offsetMinutes * 60000
  ).toISOString();
