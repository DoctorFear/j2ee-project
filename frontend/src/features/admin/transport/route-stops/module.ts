import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { routeStopSchema } from "./schema";
import { routeStopsApi } from "./api";
import { num, ref } from "../shared/forms/fields";
import { routesApi } from "../routes/api";
import { stationsApi } from "../stations/api";

export const routeStopsModule: ModuleConfig<"route-stops"> = {
  resource: "route-stops",
  api: routeStopsApi,
  defaultSort: "stopOrder",
  filterByRoute: true,
  title: transportTitles["route-stops"],
  singular: "điểm dừng",
  description: "Thiết lập thứ tự, khoảng cách và điểm đón trả trên từng tuyến.",
  columns: ["routeId", "stationId", "stopOrder", "distanceKm", "offsetMinutes", "allowPickup", "allowDropOff"],
  fields: [
    ref("routeId", routesApi),
    ref("stationId", stationsApi),
    num("stopOrder"),
    num("distanceKm", "0.01"),
    num("offsetMinutes"),
    { name: "allowPickup", kind: "checkbox" },
    { name: "allowDropOff", kind: "checkbox" },
  ],
  schema: routeStopSchema,
  defaults: {
    routeId: "",
    stationId: "",
    stopOrder: 0,
    distanceKm: 0,
    offsetMinutes: 0,
    allowPickup: true,
    allowDropOff: true,
  },
  note: "Thứ tự dừng không trùng trong cùng tuyến. Khoảng cách và thời gian tính từ điểm đầu.",
};
