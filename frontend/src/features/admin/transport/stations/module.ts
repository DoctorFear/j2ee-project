import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { stationCreateSchema, stationUpdateSchema } from "./schema";
import { stationsApi } from "./api";
import { txt, select, active } from "../shared/forms/fields";

const fields = [txt("name"), txt("address"), select("type", ["TERMINAL", "OFFICE", "ROAD_STOP", "REST_STOP"])];

export const stationsModule: ModuleConfig<"stations"> = {
  resource: "stations",
  api: stationsApi,
  title: transportTitles["stations"],
  singular: "trạm",
  description: "Quản lý bến xe, văn phòng và các điểm đón trả khách.",
  columns: ["name", "address", "type", "status"],
  fields,
  updateFields: [...fields, select("status", active)],
  schema: stationCreateSchema,
  updateSchema: stationUpdateSchema,
  defaults: { name: "", address: "", type: "TERMINAL" },
  statusOptions: active,
};
