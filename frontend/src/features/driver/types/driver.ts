export interface DriverTripStop {
  stationId: number;
  stationName: string;
  stationAddress: string;
  stopOrder: number;
  offsetMinutes: number;
  estimatedTime?: string;
  allowPickup: boolean;
  allowDropOff: boolean;
  pickupCount: number;
  dropOffCount: number;
}

export interface DriverProfile {
  id: number;
  fullName: string;
  phone: string;
  licenseNumber?: string;
  licenseClass?: string;
  status?: string;
  role?: "PRIMARY" | "RELIEF" | string;
}

export interface DriverTrip {
  tripCode: string;
  routeCode: string;
  routeName: string;
  licensePlate: string;
  vehicleType: string;
  capacity: number;
  departureAt: string;
  arrivalAt: string;
  actualDepartureAt?: string;
  actualArrivalAt?: string;
  tripStatus: "SCHEDULED" | "RUNNING" | "COMPLETED" | "CANCELLED" | string;
  driverRole: "PRIMARY" | "RELIEF" | string;
  driverId?: number;
  driverName?: string;
  driverPhone?: string;
  driverLicenseNumber?: string;
  bookedCount: number;
  boardedCount: number;
  stops: DriverTripStop[];
}

export interface DriverPassenger {
  ticketCode: string;
  seatCode: string;
  floor: string;
  customerName: string;
  customerPhone: string;
  pickupStationId?: string;
  dropOffStationId?: string;
  boarded: boolean;
  boardedAt?: string;
  ticketStatus: string;
}

export interface DriverTripSummary {
  tripCode: string;
  totalPassengers: number;
  totalBoarded: number;
  passengers: DriverPassenger[];
}

export interface CheckInRequest {
  qrToken?: string;
  ticketCode?: string;
  last4Phone?: string;
}

export interface CheckInResult {
  success: boolean;
  message: string;
  ticketCode?: string;
  seatCode?: string;
  customerName?: string;
  customerPhone?: string;
  pickupStationName?: string;
  dropOffStationName?: string;
  boardedAt?: string;
  totalBoarded?: number;
  totalPassengers?: number;
}
