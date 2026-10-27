import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { driverSchema } from "./schema";
import { driversApi } from "./api";
import { txt, date, active } from "../shared/forms/fields";

export const driversModule: ModuleConfig<"drivers"> = {
  resource: "drivers",
  api: driversApi,
  title: transportTitles["drivers"],
  singular: "tài xế",
  description: "Quản lý hồ sơ liên hệ và giấy phép lái xe.",
  columns: ["fullName", "phone", "licenseNumber", "licenseClass", "status"],
  fields: [
    txt("fullName"),
    txt("phone"),
    txt("licenseNumber", true),
    txt("licenseClass", true),
    date("licenseExpiryOn", true),
  ],
  schema: driverSchema,
  defaults: {
    fullName: "",
    phone: "",
    licenseNumber: null,
    licenseClass: null,
    licenseExpiryOn: null,
  },
  statusOptions: active,
};
