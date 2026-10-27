import type { Trip } from "../types/trip";
import { stations } from "./stations";
import { vehicleTypes } from "./vehicleTypes";

export const station = (id: string) => stations.find((s) => s.id === id)!;

// Lấy cấu hình xe của chuyến
export const vehicle = (trip: Trip) => vehicleTypes.find((v) => v.id === trip.vehicleTypeId)!;
