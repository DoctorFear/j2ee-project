import type { ActiveStatus } from "../shared/types/common";

export interface Vehicle {
  id: number;
  licensePlate: string;
  type: "SEAT" | "BERTH" | "LIMOUSINE";
  capacity: number;
  status: ActiveStatus | "MAINTENANCE";
}
