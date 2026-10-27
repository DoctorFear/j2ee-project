import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { tripDriverSchema } from "./schema";
import { tripDriversApi } from "./api";
import { ref, select } from "../shared/forms/fields";
import { tripsApi } from "../trips/api";
import { driversApi } from "../drivers/api";

export const tripDriversModule: ModuleConfig<"trip-drivers"> = {
  resource: "trip-drivers",
  api: tripDriversApi,
  title: transportTitles["trip-drivers"],
  singular: "phân công",
  description: "Bố trí tài xế chính và tài xế thay ca cho từng chuyến.",
  columns: ["tripId", "driverId", "role"],
  fields: [ref("tripId", tripsApi), ref("driverId", driversApi), select("role", ["PRIMARY", "RELIEF"])],
  schema: tripDriverSchema,
  defaults: { tripId: "", driverId: "", role: "PRIMARY" },
  note: "Một tài xế chỉ được phân công một lần trên cùng chuyến, bất kể vai trò.",
};
