import { z } from "zod";
import { text, id, money } from "../shared/validation/fields";
import { dateTime, comparableDateTime } from "../shared/validation/date-time";

export const tripSchema = z
  .object({
    code: text,
    routeId: id,
    vehicleId: id,
    departureAt: dateTime,
    arrivalAt: dateTime,
    ticketPrice: money,
  })
  .refine((v) => comparableDateTime(v.arrivalAt) > comparableDateTime(v.departureAt), {
    message: "Giờ đến phải sau giờ khởi hành",
    path: ["arrivalAt"],
  });
