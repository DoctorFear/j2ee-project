import type { Trip, TripStatistic } from "@/features/admin/transport/types";
export function aggregateStatistics(statistics: TripStatistic[], trips: Trip[]) {
  const tripMap = new Map(trips.map((trip) => [trip.id, trip]));
  const days = new Map<string, { date: string; collected: number; refunded: number }>();
  for (const stat of statistics) {
    const trip = tripMap.get(stat.tripId);
    if (!trip) continue;
    const date = trip.departureAt.slice(0, 10);
    const current = days.get(date) ?? { date, collected: 0, refunded: 0 };
    current.collected += stat.collectedAmount;
    current.refunded += stat.refundedAmount;
    days.set(date, current);
  }
  return {
    net: statistics.reduce((total, stat) => total + stat.collectedAmount - stat.refundedAmount, 0),
    tickets: statistics.reduce((total, stat) => total + stat.validTicketCount, 0),
    days: [...days.values()].sort((a, b) => a.date.localeCompare(b.date)).slice(-14),
    missingTrips: statistics.filter((stat) => !tripMap.has(stat.tripId)).length,
  };
}
