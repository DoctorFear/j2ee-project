import { z } from "zod";
import { id, int, money } from "../shared/validation/fields";

export const statisticSchema = z.object({
  tripId: id,
  validTicketCount: int,
  collectedAmount: money,
  refundedAmount: money,
});
