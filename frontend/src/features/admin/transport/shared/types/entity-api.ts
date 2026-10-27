import type { EntityMap, Resource } from "./resource";
import type { FormValues } from "./form";
export interface EntityListOptions {
  routeId?: number;
  enabled?: boolean;
}
export interface EntityApi<K extends Resource = Resource> {
  resource: K;
  list: (options?: EntityListOptions & { signal?: AbortSignal }) => Promise<EntityMap[K][]>;
  detail: (id: number, signal?: AbortSignal) => Promise<EntityMap[K]>;
  create: (values: FormValues) => Promise<EntityMap[K]>;
  update?: (id: number, values: FormValues) => Promise<EntityMap[K]>;
  remove: (id: number) => Promise<void>;
}
