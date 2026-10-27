// Tính giá vé theo chặng và cơ chế định giá
import type { Trip } from "@/features/customer/types/trip";
import { segment } from "@/features/customer/model/trip";
import type { CustomerJourney } from "@/features/customer/types/journey";

// Tính giá theo chặng và khuyến mãi, làm tròn đến nghìn đồng
export function fare(trip: Trip, originId: string, destinationId: string) {
  const part = segment(trip, originId, destinationId);
  if (!part) return 0;
  if ("unitFare" in trip) return (trip as CustomerJourney).unitFare;
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
