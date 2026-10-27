import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { routeSchema } from "./schema";
import { routesApi } from "./api";
import { txt, num, date, active } from "../shared/forms/fields";

export const routesModule: ModuleConfig<"routes"> = {
  resource: "routes",
  api: routesApi,
  title: transportTitles["routes"],
  singular: "tuyến",
  description: "Quản lý tuyến đường, phiên bản khai thác và giá cơ bản.",
  columns: ["code", "name", "versionNo", "basePrice", "status"],
  fields: [
    txt("code"),
    txt("name"),
    num("versionNo"),
    num("basePrice"),
    date("effectiveFromAt"),
    date("effectiveToAt", true),
  ],
  schema: routeSchema,
  defaults: {
    code: "",
    name: "",
    versionNo: 1,
    basePrice: "",
    effectiveFromAt: "",
    effectiveToAt: null,
  },
  statusOptions: active,
  note: "Mỗi mã tuyến và phiên bản là duy nhất. Xóa các điểm dừng trước khi xóa tuyến.",
};
