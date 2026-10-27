export interface RouteStop {
  id: number;
  routeId: number;
  stationId: number;
  stopOrder: number;
  distanceKm: number;
  offsetMinutes: number;
  allowPickup: boolean;
  allowDropOff: boolean;
}
