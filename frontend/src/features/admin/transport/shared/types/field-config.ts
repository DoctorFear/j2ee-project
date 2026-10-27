import type { EntityApi } from "./entity-api";

export interface FieldConfig {
  name: string;
  label?: string;
  kind?: "text" | "number" | "datetime-local" | "select" | "checkbox";
  options?: string[];
  relation?: EntityApi;
  optional?: boolean;
  step?: string;
}
