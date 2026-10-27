import { z } from "zod";
import { text, int } from "../shared/validation/fields";

export const vehicleSchema = z.object({
  licensePlate: text,
  type: z.enum(["SEAT", "BERTH", "LIMOUSINE"]),
  capacity: int.min(1, "Sức chứa phải lớn hơn 0"),
});
