// Thông tin chuyến và danh mục lấy từ dịch vụ đặt vé
import type { Trip } from "../../../types/trip";
import type { Station } from "../../../types/station";
import type { VehicleType } from "../../../types/vehicle";
export type CustomerJourney = Trip & {
  stations: Station[];
  vehicle: VehicleType;
  unitFare: number;
  arrivalAt: string;
  status: string;
  bookable: boolean;
};
export type JourneyResponse = {
  code: string;
  routeName: string;
  departureAt: string;
  arrivalAt: string;
  status: string;
  saleStatus: string;
  ticketPrice: number;
  bookable: boolean;
  stops: (Trip["stops"][number] & {
    allowPickup: boolean;
    allowDropOff: boolean;
  })[];
  stations: Station[];
  vehicle: VehicleType;
  soldSeats: string[];
  heldSeats: string[];
};
