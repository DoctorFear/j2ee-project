import { z } from "zod";
import { text, optionalText } from "../shared/validation/fields";
import { dateOnlyString } from "../shared/validation/date-time";

export const driverSchema = z.object({
  fullName: text,
  phone: text,
  licenseNumber: optionalText,
  licenseClass: optionalText,
  licenseExpiryOn: dateOnlyString.nullable(),
});

export const driverUpdateSchema = driverSchema.extend({
  status: z.enum(["ACTIVE", "INACTIVE"]),
});
