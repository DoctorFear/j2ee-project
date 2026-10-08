// Thông tin chuyến xe dùng chung các phân hệ
import type { RouteStop } from "./route-stop";
import type { Pricing } from "./pricing";
export type Trip = {
  rating: number;
  code: string;
  routeName: string;
  departureAt: string;
  stops: RouteStop[];
  vehicleTypeId: string;
  pricePerKm: number;
  pricing: Pricing;
  saleStatus: "OPEN" | "PAUSED";
  soldSeats: string[];
  heldSeats: string[];
};
