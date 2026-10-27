import { Link } from "react-router-dom";
import { fieldRelation } from "../model/field-relation";
import { LoadingState, ErrorState, EmptyState } from "@/components/shared/AsyncState";
import { DataTable } from "@/components/shared/DataTable";
import { useEntityList } from "@/features/admin/transport/shared/hooks/useEntityList";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { TransportEntity } from "@/features/admin/transport/types";
import { labels } from "@/features/admin/transport/shared/constants/field-labels";
import { readField } from "@/features/admin/transport/shared/model/read-field";
import type { ModuleConfig } from "@/features/admin/transport/shared/types/module-config";
import { EntityValue } from "@/features/admin/transport/shared/components/EntityValue";

export function RelatedRecords({
  config,
  routeId,
  filter,
  manageTo,
}: {
  config: ModuleConfig;
  routeId?: number;
  filter?: (entity: TransportEntity) => boolean;
  manageTo?: string;
}) {
  const query = useEntityList(config.api, { routeId });
  const records = filter ? query.data?.filter(filter) : query.data;
  return (
    <section className="admin-panel mt-6">
      <div className="admin-panel-heading">
        <h2>{config.title}</h2>
        <Link className="text-xs font-medium text-primary" to={manageTo ?? "/admin/" + config.resource}>
          Quản lý →
        </Link>
      </div>
      {query.isPending ? (
        <LoadingState />
      ) : query.isError ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : !records?.length ? (
        <EmptyState message="Chưa có dữ liệu liên quan" detail="Thêm bản ghi tại màn hình quản lý tương ứng." />
      ) : (
        <DataTable
          caption={config.title}
          rows={records}
          rowKey={entityId}
          columns={config.columns.map((field) => ({
            key: field,
            label: labels[field],
            render: (row: TransportEntity) => (
              <EntityValue field={field} value={readField(row, field)} relation={fieldRelation(config, field)} />
            ),
          }))}
        />
      )}
    </section>
  );
}
