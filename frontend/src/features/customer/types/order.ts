// Trạng thái đơn giữ chỗ và phiên thanh toán
export type BookingOrder = {
  id: string;
  tripCode: string;
  status: string;
  expiresAt: string;
  serverTime: string;
  amount: number;
  fare: number;
  seatCodes: string[];
  originId: string;
  destinationId: string;
  contact: { name: string; phone: string; email: string };
};
export type PaymentSession = {
  id: string;
  payUrl: string;
  qrCodeUrl: string;
  amount: number;
};
export type RefundStatus = {
  ticketCode: string;
  status: string;
  requestedAt: string;
};
