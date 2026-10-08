// Tính giá vé theo chặng và cơ chế định giá
import type { Trip } from "../types/trip";
import { segment } from "./trip";

// Tính giá theo chặng và khuyến mãi, làm tròn đến nghìn đồng
export function fare(trip: Trip, originId: string, destinationId: string) {
  const part = segment(trip, originId, destinationId);
  if (!part) return 0;
  const distance = part.destination.distanceKm - part.origin.distanceKm;
  const total = trip.stops.at(-1)!.distanceKm;
  const base =
    trip.pricing.mode === "FIXED"
      ? (trip.pricing.fullRoutePrice * distance) / total
      : distance * trip.pricePerKm;
  return (
    Math.round(
      (base *
        (trip.pricing.mode === "DISCOUNT"
          ? 1 - trip.pricing.percent / 100
          : 1)) /
        1000,
    ) * 1000
  );
}
