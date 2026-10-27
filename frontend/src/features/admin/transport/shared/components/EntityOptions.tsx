import { NativeSelectOption } from "@/components/ui/native-select";
import { useEntityList } from "@/features/admin/transport/shared/hooks/useEntityList";
import { entityLabel } from "@/features/admin/transport/shared/model/entity-label";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { EntityApi } from "../types/entity-api";

export function EntityOptions({ api }: { api: EntityApi }) {
  const query = useEntityList(api);
  return (
    <>
      {query.isPending ? (
        <option disabled>Đang tải…</option>
      ) : query.isError ? (
        <option disabled>Không thể tải danh sách</option>
      ) : query.data.length === 0 ? (
        <option disabled>Chưa có dữ liệu liên quan</option>
      ) : (
        query.data.map((entity) => (
          <NativeSelectOption key={entityId(entity)} value={entityId(entity)}>
            {entityLabel(entity)}
          </NativeSelectOption>
        ))
      )}
    </>
  );
}
