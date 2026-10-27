import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { tripSchema } from "./schema";
import { tripsApi } from "./api";
import { txt, num, date, ref } from "../shared/forms/fields";
import { routesApi } from "../routes/api";
import { vehiclesApi } from "../vehicles/api";

export const tripsModule: ModuleConfig<"trips"> = {
  resource: "trips",
  api: tripsApi,
  title: transportTitles["trips"],
  singular: "chuyến xe",
  description: "Lập lịch vận hành, chọn tuyến đường và phương tiện cho chuyến.",
  columns: ["code", "routeId", "vehicleId", "departureAt", "ticketPrice", "status", "saleStatus"],
  fields: [
    txt("code"),
    ref("routeId", routesApi),
    ref("vehicleId", vehiclesApi),
    date("departureAt"),
    date("arrivalAt"),
    num("ticketPrice"),
  ],
  schema: tripSchema,
  defaults: {
    code: "",
    routeId: "",
    vehicleId: "",
    departureAt: "",
    arrivalAt: "",
    ticketPrice: "",
  },
  statusOptions: ["DRAFT", "SCHEDULED", "RUNNING", "COMPLETED", "CANCELLED"],
  note: "Chuyến mới ở trạng thái bản nháp, chưa mở bán. Sức chứa được sao chép từ xe đã chọn.",
};
