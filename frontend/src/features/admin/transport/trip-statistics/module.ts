import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { statisticSchema } from "./schema";
import { tripStatisticsApi } from "./api";
import { num, ref } from "../shared/forms/fields";
import { tripsApi } from "../trips/api";

export const tripStatisticsModule: ModuleConfig<"trip-statistics"> = {
  resource: "trip-statistics",
  api: tripStatisticsApi,
  title: transportTitles["trip-statistics"],
  singular: "bản thống kê",
  description: "Đối soát số vé hợp lệ, tiền thu và hoàn theo từng chuyến xe.",
  columns: ["tripId", "validTicketCount", "collectedAmount", "refundedAmount", "updatedAt"],
  fields: [ref("tripId", tripsApi), num("validTicketCount"), num("collectedAmount"), num("refundedAmount")],
  schema: statisticSchema,
  defaults: { tripId: "", validTicketCount: 0, collectedAmount: 0, refundedAmount: 0 },
  note: "Mỗi chuyến chỉ có một bản thống kê. API hiện hỗ trợ tạo và xóa bản ghi, chưa hỗ trợ cập nhật.",
};
