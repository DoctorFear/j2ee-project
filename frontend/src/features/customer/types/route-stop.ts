// Thông tin trạm dừng trong lộ trình
export type RouteStop = {
  stationId: string;
  order: number;
  distanceKm: number;
  offsetMinutes: number;
  active: boolean;
};
