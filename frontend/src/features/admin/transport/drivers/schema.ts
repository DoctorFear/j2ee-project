import { z } from "zod";
import { text, optionalText } from "../shared/validation/fields";
import { dateTime } from "../shared/validation/date-time";

export const driverSchema = z.object({
  fullName: text,
  phone: text,
  licenseNumber: optionalText,
  licenseClass: optionalText,
  licenseExpiryOn: dateTime.nullable(),
});
