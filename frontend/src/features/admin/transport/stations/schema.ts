import { z } from "zod";
import { text } from "../shared/validation/fields";

export const stationCreateSchema = z.object({
  name: text,
  address: text,
  type: z.enum(["TERMINAL", "OFFICE", "ROAD_STOP", "REST_STOP"]),
});
export const stationUpdateSchema = stationCreateSchema.extend({
  status: z.enum(["ACTIVE", "INACTIVE"]),
});
