// Thông tin vé điện tử dùng chung các phân hệ
import type { Contact } from "./contact";
export type Ticket = {
  code: string;
  tripCode: string;
  seatCode: string;
  originId: string;
  destinationId: string;
  passenger: Contact;
  fare: number;
  fee: number;
  paymentMethod: "MOMO";
  status: "PAID" | "COMPLETED" | "CANCELLED";
  qrToken: string;
};
