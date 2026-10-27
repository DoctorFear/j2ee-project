import type { ActiveStatus } from "../shared/types/common";

export interface TransportRoute {
  id: number;
  code: string;
  versionNo: number;
  name: string;
  basePrice: number;
  effectiveFromAt: string;
  effectiveToAt: string | null;
  status: ActiveStatus;
}
