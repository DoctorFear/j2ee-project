// Dữ liệu vé và thông tin khách hàng
import type { Ticket } from "../types/ticket";
import { trips } from "./demoTrips";
import { localDate } from "../utils/localDate";
export const demoContact = {
  name: "Nguyễn Trương Mai Phương",
  phone: "0912345678",
  email: "maiphuong@example.com",
};

// Tạo danh sách vé sắp khởi hành và lịch sử
export const tickets: Ticket[] = [0, -2].map((day, i) => ({
  code: `FUTA-DEMO-${i + 1}`,
  tripCode: trips.find(
    (t) => t.departureAt.startsWith(localDate(day)) && t.code.endsWith("D3"),
  )!.code,
  seatCode: "B03",
  originId: "AN-NHON",
  destinationId: "MIEN-TAY",
  passenger: demoContact,
  fare: 390000,
  fee: 0,
  paymentMethod: "MOMO",
  status: i ? "COMPLETED" : "PAID",
  qrToken: `demo-ticket-${i + 1}`,
}));
