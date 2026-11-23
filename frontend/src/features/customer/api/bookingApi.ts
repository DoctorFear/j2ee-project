// Gọi API danh mục, đặt chỗ, thanh toán và vé điện tử
import { apiClient } from "@/api/client";
import type {
  CustomerJourney,
  JourneyResponse,
} from "@/features/customer/types/journey";
import type {
  BookingOrder,
  PaymentSession,
  RefundStatus,
} from "@/features/customer/types/order";
import type { Contact } from "@/features/customer/types/contact";
import type { Station } from "@/features/customer/types/station";
import type { Ticket } from "@/features/customer/types/ticket";

// Chuyển dữ liệu API sang cấu trúc đang dùng của giao diện
export function journeyFromApi(raw: JourneyResponse): CustomerJourney {
  return {
    ...raw,
    saleStatus: raw.saleStatus === "OPEN" ? "OPEN" : "PAUSED",
    rating: 0,
    vehicleTypeId: raw.vehicle.name,
    pricePerKm: 0,
    pricing: { mode: "FIXED", fullRoutePrice: raw.ticketPrice },
    unitFare: raw.ticketPrice,
  };
}
export type CustomerTicket = Ticket & {
  trip: CustomerJourney;
  pickupAt: string;
  arrivalAt: string;
};
export const bookingApi = {
  stations: () => apiClient<Station[]>("/bookings/catalog/stations"),
  trips: async () =>
    (await apiClient<JourneyResponse[]>("/bookings/catalog/trips")).map(
      journeyFromApi,
    ),
  trip: async (code: string) =>
    journeyFromApi(
      await apiClient<JourneyResponse>(
        `/bookings/catalog/trips/${encodeURIComponent(code)}`,
      ),
    ),
  hold: (
    tripCode: string,
    originId: string,
    destinationId: string,
    seatCodes: string[],
    contact: Contact,
  ) =>
    apiClient<BookingOrder>("/bookings/hold", {
      method: "POST",
      data: {
        tripCode,
        originId,
        destinationId,
        seatCodes,
        ...contact,
      },
    }),
  order: (id: string) =>
    apiClient<BookingOrder>(`/bookings/${encodeURIComponent(id)}`),
  release: (id: string) =>
    apiClient<boolean>(`/bookings/${encodeURIComponent(id)}/hold`, {
      method: "DELETE",
    }),
  pay: (id: string) =>
    apiClient<PaymentSession>(`/bookings/${encodeURIComponent(id)}/pay`, {
      method: "POST",
    }),
  tickets: async () =>
    (
      await apiClient<
        (Ticket & {
          trip: JourneyResponse;
          pickupAt: string;
          arrivalAt: string;
        })[]
      >("/tickets/my")
    ).map((t) => ({ ...t, trip: journeyFromApi(t.trip) })),
  cancel: (code: string) =>
    apiClient<RefundStatus>(`/tickets/${encodeURIComponent(code)}/cancel`, {
      method: "POST",
    }),
  refund: (code: string) =>
    apiClient<RefundStatus | null>(
      `/tickets/${encodeURIComponent(code)}/refund`,
    ),
};
