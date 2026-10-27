import type { FieldConfig } from "../types/field-config";
import type { EntityApi } from "../types/entity-api";

export const txt = (name: string, optional = false): FieldConfig => ({ name, optional });
export const num = (name: string, step = "1"): FieldConfig => ({ name, kind: "number", step });
export const date = (name: string, optional = false): FieldConfig => ({
  name,
  kind: "datetime-local",
  optional,
});
export const ref = (name: string, relation: EntityApi): FieldConfig => ({
  name,
  kind: "select",
  relation,
});
export const select = (name: string, options: string[]): FieldConfig => ({
  name,
  kind: "select",
  options,
});
export const active = ["ACTIVE", "INACTIVE"];
