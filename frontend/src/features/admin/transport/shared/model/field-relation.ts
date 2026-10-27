import type { ModuleConfig } from "../types/module-config";
export function fieldRelation(config: ModuleConfig, name: string) {
  return config.fields.find((field) => field.name === name)?.relation;
}
