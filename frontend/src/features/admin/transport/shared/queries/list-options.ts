import { queryOptions } from "@tanstack/react-query";
import type { EntityApi, EntityListOptions } from "../types/entity-api";
import type { Resource } from "../types/resource";
import { transportKeys } from "./keys";
export function entityListOptions<K extends Resource>(api: EntityApi<K>, options: EntityListOptions = {}) {
  return queryOptions({
    queryKey: transportKeys.list(api.resource, options.routeId),
    queryFn: ({ signal }) => api.list({ routeId: options.routeId, signal }),
    staleTime: 60000,
  });
}
