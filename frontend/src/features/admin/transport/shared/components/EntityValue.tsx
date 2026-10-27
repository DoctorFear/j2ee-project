import { Link } from "react-router-dom";
import { useEntityList } from "@/features/admin/transport/shared/hooks/useEntityList";
import { entityLabel } from "@/features/admin/transport/shared/model/entity-label";
import { formatValue } from "@/features/admin/transport/shared/format/format-value";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { EntityApi } from "../types/entity-api";
import { StatusBadge } from "@/features/admin/transport/shared/components/StatusBadge";

function RelationValue({ api, id }: { api: EntityApi; id: number }) {
  const query = useEntityList(api);
  const resource = api.resource;
  const entity = query.data?.find((record) => entityId(record) === id);
  return (
    <Link
      className="font-medium text-primary hover:underline"
      to={`/admin/${resource}/${id}`}
      title={query.error?.message}
    >
      {entity ? entityLabel(entity) : `#${id}`}
      {query.isError && <span className="ml-1 text-xs text-destructive">(chưa tải được tên)</span>}
    </Link>
  );
}
export function EntityValue({ field, value, relation }: { field: string; value: unknown; relation?: EntityApi }) {
  if (value == null) return <span className="text-muted-foreground">—</span>;
  if (relation && typeof value === "number") return <RelationValue api={relation} id={value} />;
  if (["status", "saleStatus"].includes(field)) return <StatusBadge value={String(value)} />;
  return <>{formatValue(field, value)}</>;
}
