import { useQuery } from "@tanstack/react-query";
import type { EntityApi, EntityListOptions } from "../types/entity-api";
import type { Resource } from "../types/resource";
import { entityListOptions } from "../queries/list-options";
export function useEntityList<K extends Resource>(api: EntityApi<K>, options: EntityListOptions = {}) {
  return useQuery({ ...entityListOptions(api, options), enabled: options.enabled });
}
