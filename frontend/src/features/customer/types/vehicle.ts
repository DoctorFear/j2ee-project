// Thông tin loại xe và cấu hình ghế
import type { Seat } from "@/features/customer/types/seat";
export type VehicleType = { id: string; name: string; seats: Seat[] };
