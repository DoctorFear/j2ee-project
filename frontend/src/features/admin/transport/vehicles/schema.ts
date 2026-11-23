import { z } from "zod";
import { text, int } from "../shared/validation/fields";

export const vehicleSchema = z.object({
  licensePlate: text,
  type: z.enum(["SEAT", "BERTH", "LIMOUSINE"]),
  capacity: int.min(1, "Sức chứa phải lớn hơn 0"),
});

export const vehicleUpdateSchema = vehicleSchema.extend({
  status: z.enum(["ACTIVE", "MAINTENANCE", "INACTIVE"]),
});
