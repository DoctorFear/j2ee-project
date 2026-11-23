import type { ModuleConfig } from "../shared/types/module-config";
import { transportTitles } from "../navigation";
import { driverSchema, driverUpdateSchema } from "./schema";
import { driversApi } from "./api";
import { txt, dateOnly, select, active } from "../shared/forms/fields";

const fields = [
  txt("fullName"),
  txt("phone"),
  txt("licenseNumber", true),
  txt("licenseClass", true),
  dateOnly("licenseExpiryOn", true),
];

export const driversModule: ModuleConfig<"drivers"> = {
  resource: "drivers",
  api: driversApi,
  title: transportTitles["drivers"],
  singular: "tài xế",
  description: "Quản lý hồ sơ liên hệ và giấy phép lái xe.",
  columns: ["fullName", "phone", "licenseNumber", "licenseClass", "status"],
  fields,
  updateFields: [...fields, select("status", active)],
  schema: driverSchema,
  updateSchema: driverUpdateSchema,
  defaults: {
    fullName: "",
    phone: "",
    licenseNumber: null,
    licenseClass: null,
    licenseExpiryOn: null,
  },
  statusOptions: active,
};
