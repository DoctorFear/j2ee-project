import { z } from "zod";
import { text, int, money } from "../shared/validation/fields";
import { dateTime, comparableDateTime } from "../shared/validation/date-time";

export const routeSchema = z
  .object({
    code: text,
    versionNo: int.min(1, "Phiên bản phải lớn hơn 0"),
    name: text,
    basePrice: money,
    effectiveFromAt: dateTime,
    effectiveToAt: dateTime.nullable(),
  })
  .refine((v) => !v.effectiveToAt || comparableDateTime(v.effectiveToAt) > comparableDateTime(v.effectiveFromAt), {
    message: "Ngày kết thúc phải sau ngày bắt đầu",
    path: ["effectiveToAt"],
  });
