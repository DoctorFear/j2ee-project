import type { Trip } from "@/features/admin/transport/types";
import { enumLabels } from "@/features/admin/transport/shared/constants/enum-labels";

const statusColors = {
  DRAFT: "#94a3b8",
  SCHEDULED: "#4c8df6",
  RUNNING: "#f47748",
  COMPLETED: "#31b58b",
  CANCELLED: "#e57777",
};

export interface TripStatusGroup {
  status: string;
  name: string;
  value: number;
  color: string;
}

export function selectTripOverview(trips: readonly Trip[], localNow: string) {
  const statuses: TripStatusGroup[] = Object.entries(statusColors)
    .map(([status, color]) => ({
      status,
      name: enumLabels[status],
      value: trips.filter((trip) => trip.status === status).length,
      color,
    }))
    .filter((group) => group.value > 0);
  const futureTrips = trips
    .filter((trip) => trip.departureAt > localNow && !["CANCELLED", "COMPLETED"].includes(trip.status))
    .sort((a, b) => a.departureAt.localeCompare(b.departureAt))
    .slice(0, 5);
  return { statuses, futureTrips };
}
