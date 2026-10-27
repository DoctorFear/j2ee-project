import type { Resource } from "./shared/types/resource";

export const transportTitles: Record<Resource, string> = {
  stations: "Trạm & bến xe",
  routes: "Tuyến vận tải",
  "route-stops": "Lộ trình & điểm dừng",
  vehicles: "Phương tiện",
  drivers: "Tài xế",
  trips: "Lịch chuyến",
  "trip-drivers": "Phân công tài xế",
  "trip-statistics": "Thống kê chuyến",
};
