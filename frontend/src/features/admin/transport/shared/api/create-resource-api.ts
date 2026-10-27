import { apiClient } from "@/api/client";
import type { EntityApi, EntityListOptions } from "../types/entity-api";
import type { EntityMap, Resource } from "../types/resource";
export function createResourceApi<K extends Resource>(
  resource: K,
  listParams?: (options: EntityListOptions) => Record<string, number> | undefined
): EntityApi<K> {
  const endpoint = "/api/" + resource;
  return {
    resource,
    list: (options = {}) =>
      apiClient<EntityMap[K][]>(endpoint, {
        params: listParams?.(options),
        signal: options.signal,
      }),
    detail: (id, signal) => apiClient<EntityMap[K]>(endpoint + "/" + id, { signal }),
    create: (values) => apiClient<EntityMap[K]>(endpoint, { method: "POST", data: values }),
    remove: (id) => apiClient<void>(endpoint + "/" + id, { method: "DELETE" }),
  };
}
