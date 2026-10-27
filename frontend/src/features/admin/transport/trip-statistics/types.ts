export interface TripStatistic {
  tripId: number;
  validTicketCount: number;
  collectedAmount: number;
  refundedAmount: number;
  updatedAt: string | null;
}
