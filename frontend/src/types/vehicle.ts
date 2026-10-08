// Thông tin loại xe và cấu hình ghế
import type { Seat } from "./seat";
export type VehicleType = { id: string; name: string; seats: Seat[] };
