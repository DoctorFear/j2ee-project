export interface TripDriver {
  id: number;
  tripId: number;
  driverId: number;
  role: "PRIMARY" | "RELIEF";
}
