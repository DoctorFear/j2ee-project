import { z } from "zod";
import { id } from "../shared/validation/fields";

export const tripDriverSchema = z.object({
  tripId: id,
  driverId: id,
  role: z.enum(["PRIMARY", "RELIEF"]),
});
