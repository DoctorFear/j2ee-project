import { useQuery } from "@tanstack/react-query";
import type { EntityApi } from "../types/entity-api";
import type { Resource } from "../types/resource";
import { transportKeys } from "../queries/keys";
export function useEntityDetail<K extends Resource>(api: EntityApi<K>, id: number) {
  return useQuery({
    queryKey: transportKeys.detail(api.resource, id),
    queryFn: ({ signal }) => api.detail(id, signal),
    enabled: Number.isSafeInteger(id) && id > 0,
  });
}
