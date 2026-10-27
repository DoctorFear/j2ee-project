import type { ActiveStatus } from "../shared/types/common";

export interface Driver {
  id: number;
  fullName: string;
  phone: string;
  licenseNumber: string | null;
  licenseClass: string | null;
  licenseExpiryOn: string | null;
  status: ActiveStatus;
}
