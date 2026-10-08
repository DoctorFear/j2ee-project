// Thông tin đặt chỗ đang nhập của khách hàng
import type { Contact } from "../../../types/contact";
export type BookingDraft = {
  tripCode: string;
  originId: string;
  destinationId: string;
  seatCodes: string[];
  contact: Contact;
};
