export interface Trip {
  id: number;
  code: string;
  routeId: number;
  vehicleId: number;
  departureAt: string;
  arrivalAt: string;
  estimatedDepartureAt: string | null;
  estimatedArrivalAt: string | null;
  actualDepartureAt: string | null;
  actualArrivalAt: string | null;
  ticketPrice: number;
  capacitySnapshot: number;
  status: "DRAFT" | "SCHEDULED" | "RUNNING" | "COMPLETED" | "CANCELLED";
  saleStatus: "NOT_OPEN" | "OPEN" | "PAUSED" | "CLOSED";
}
