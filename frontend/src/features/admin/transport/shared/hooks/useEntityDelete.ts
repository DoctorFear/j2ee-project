import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { EntityApi } from "../types/entity-api";
import { transportKeys } from "../queries/keys";
export function useEntityDelete(api: EntityApi) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.remove(id),
    onSuccess: (_data, id) => {
      client.removeQueries({ queryKey: transportKeys.detail(api.resource, id) });
      return client.invalidateQueries({ queryKey: transportKeys.all });
    },
  });
}
