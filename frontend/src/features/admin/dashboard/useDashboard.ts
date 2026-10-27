import { tripStatisticsApi } from "@/features/admin/transport/trip-statistics/api";
import { routesApi } from "@/features/admin/transport/routes/api";
import { driversApi } from "@/features/admin/transport/drivers/api";
import { vehiclesApi } from "@/features/admin/transport/vehicles/api";
import { tripsApi } from "@/features/admin/transport/trips/api";
import { useEntityList } from "@/features/admin/transport/shared/hooks/useEntityList";
import { aggregateStatistics } from "@/features/admin/dashboard/aggregate";
import { selectTripOverview } from "@/features/admin/dashboard/trip-overview";
import { useClock, vietnamLocalDateTime } from "@/lib/useClock";

export function useDashboard() {
  const now = useClock();
  const trips = useEntityList(tripsApi);
  const vehicles = useEntityList(vehiclesApi);
  const drivers = useEntityList(driversApi);
  const routes = useEntityList(routesApi);
  const statistics = useEntityList(tripStatisticsApi);
  const queries = [trips, vehicles, drivers, routes, statistics];
  const aggregate = aggregateStatistics(statistics.data ?? [], trips.data ?? []);
  const overview = selectTripOverview(trips.data ?? [], vietnamLocalDateTime(now));
  const lastSync = Math.min(...queries.filter((query) => query.isSuccess).map((query) => query.dataUpdatedAt));
  const refresh = () => Promise.all(queries.map((query) => query.refetch()));
  return {
    trips,
    vehicles,
    drivers,
    routes,
    statistics,
    queries,
    aggregate,
    ...overview,
    lastSync,
    refresh,
    fetching: queries.some((query) => query.isFetching),
  };
}
