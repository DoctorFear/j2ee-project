import type { ActiveStatus } from "../shared/types/common";

export type StationType = "TERMINAL" | "OFFICE" | "ROAD_STOP" | "REST_STOP";

export interface Station {
  id: number;
  name: string;
  address: string;
  type: StationType;
  status: ActiveStatus;
}
