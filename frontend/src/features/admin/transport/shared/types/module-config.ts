import type { z } from "zod";
import type { EntityApi } from "./entity-api";
import type { FieldConfig } from "./field-config";
import type { FormValues } from "./form";
import type { Resource } from "./resource";

export interface ModuleConfig<K extends Resource = Resource> {
  resource: K;
  api: EntityApi<K>;
  defaultSort?: string;
  filterByRoute?: boolean;
  updateFields?: FieldConfig[];
  title: string;
  singular: string;
  description: string;
  columns: string[];
  fields: FieldConfig[];
  schema: z.ZodType<FormValues>;
  updateSchema?: z.ZodType<FormValues>;
  defaults: FormValues;
  statusOptions?: string[];
  note?: string;
}
