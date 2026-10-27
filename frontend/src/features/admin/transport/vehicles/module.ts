import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { vehicleSchema } from "./schema";
import { vehiclesApi } from "./api";
import { txt, num, select } from "../shared/forms/fields";

export const vehiclesModule: ModuleConfig<"vehicles"> = {
  resource: "vehicles",
  api: vehiclesApi,
  title: transportTitles["vehicles"],
  singular: "phương tiện",
  description: "Theo dõi đội xe, loại dịch vụ và sức chứa khai thác.",
  columns: ["licensePlate", "type", "capacity", "status"],
  fields: [txt("licensePlate"), select("type", ["SEAT", "BERTH", "LIMOUSINE"]), num("capacity")],
  schema: vehicleSchema,
  defaults: { licensePlate: "", type: "BERTH", capacity: "" },
  statusOptions: ["ACTIVE", "MAINTENANCE", "INACTIVE"],
};
