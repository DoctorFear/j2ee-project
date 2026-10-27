import { z } from "zod";
import { id, int } from "../shared/validation/fields";

export const routeStopSchema = z.object({
  routeId: id,
  stationId: id,
  stopOrder: int,
  distanceKm: z
    .number({ error: "Vui lòng nhập khoảng cách" })
    .min(0, "Không được âm")
    .max(99999999.99, "Tối đa 8 chữ số nguyên")
    .refine((v) => Math.abs(v * 100 - Math.round(v * 100)) < 0.00001, "Tối đa 2 chữ số thập phân"),
  offsetMinutes: int,
  allowPickup: z.boolean(),
  allowDropOff: z.boolean(),
});
