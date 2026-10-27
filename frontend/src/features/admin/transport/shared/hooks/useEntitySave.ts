import { useMutation, useQueryClient } from "@tanstack/react-query";
import type { EntityApi } from "../types/entity-api";
import type { FormValues } from "../types/form";
import { transportKeys } from "../queries/keys";
export function useEntitySave(api: EntityApi, id?: number) {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (values: FormValues) => {
      if (id === undefined) return api.create(values);
      if (!api.update) throw new Error("Tài nguyên này chưa hỗ trợ cập nhật.");
      return api.update(id, values);
    },
    onSuccess: () => client.invalidateQueries({ queryKey: transportKeys.all }),
  });
}
