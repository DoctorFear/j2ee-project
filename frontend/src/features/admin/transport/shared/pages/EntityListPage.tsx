import { EntityListFilters } from "../components/EntityListFilters";
import { EntityRowActions } from "../components/EntityRowActions";
import { PageHeading } from "@/components/shared/PageHeading";
import { useEntityListPage } from "@/features/admin/transport/shared/hooks/useEntityListPage";
import { useState, type ReactNode } from "react";
import { Plus, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LoadingState, ErrorState } from "@/components/shared/AsyncState";
import { DataTable, TablePagination } from "@/components/shared/DataTable";
import { labels } from "@/features/admin/transport/shared/constants/field-labels";
import { readField } from "@/features/admin/transport/shared/model/read-field";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { TransportEntity } from "../../types";
import type { ModuleConfig } from "../types/module-config";
import { fieldRelation } from "../model/field-relation";
import { EntityValue } from "@/features/admin/transport/shared/components/EntityValue";
import { EntityFormDialog } from "@/features/admin/transport/shared/components/EntityFormDialog";
import { DeleteDialog } from "@/features/admin/transport/shared/components/DeleteDialog";

export function EntityListPage({ config, filters }: { config: ModuleConfig; filters?: ReactNode }) {
  const [form, setForm] = useState<number | "create" | null>(null);
  const [deleting, setDeleting] = useState<TransportEntity | null>(null);
  const [notice, setNotice] = useState("");

  const { query, q, status, sortKey, desc, pageSize, page, routeId, rows, total, change } = useEntityListPage(config);

  const complete = (message: string) => {
    setForm(null);
    setDeleting(null);
    setNotice(message);
  };

  return (
    <>
      <PageHeading
        className="admin-page-heading"
        title={config.title}
        description={<>{config.description}</>}
        actions={
          <Button onClick={() => setForm("create")}>
            <Plus size={16} />
            Thêm {config.singular}
          </Button>
        }
      />
      {notice && (
        <div
          role="status"
          className="mb-4 flex items-center justify-between rounded-lg border border-emerald-600/20 bg-emerald-500/10 px-4 py-3 text-emerald-700 dark:text-emerald-300"
        >
          {notice}
          <Button variant="ghost" size="sm" onClick={() => setNotice("")}>
            Đóng
          </Button>
        </div>
      )}
      <section className="admin-panel">
        <div className="admin-panel-heading">
          <div>
            <h2>Danh sách {config.singular}</h2>
            <p>
              {query.isSuccess
                ? `${query.data.length} bản ghi${routeId ? " trên tuyến đã chọn" : ""}`
                : "Dữ liệu vận hành"}
            </p>
          </div>
          <Button variant="outline" size="sm" disabled={query.isFetching} onClick={() => void query.refetch()}>
            <RefreshCw size={14} className={query.isFetching ? "animate-spin" : ""} />
            Làm mới
          </Button>
        </div>
        <EntityListFilters config={config} q={q} status={status} change={change} filters={filters} />
        {query.isError ? (
          <ErrorState error={query.error} retry={() => void query.refetch()} />
        ) : query.isPending ? (
          <LoadingState />
        ) : (
          <>
            <DataTable
              rows={rows}
              rowKey={entityId}
              caption={config.title}
              columns={config.columns.map((field) => ({
                key: field,
                label: labels[field],
                render: (row: TransportEntity) => (
                  <EntityValue field={field} value={readField(row, field)} relation={fieldRelation(config, field)} />
                ),
              }))}
              sort={{ key: sortKey, desc }}
              onSort={(key) => change({ sort: key, direction: sortKey === key && !desc ? "desc" : "asc" })}
              actions={(row) => (
                <EntityRowActions
                  config={config}
                  row={row}
                  onEdit={() => setForm(entityId(row))}
                  onDelete={() => setDeleting(row)}
                />
              )}
            />
            <TablePagination
              page={page}
              pageSize={pageSize}
              total={total}
              onPage={(p) => change({ page: String(p) }, false)}
              onPageSize={(s) => change({ size: String(s) })}
            />
          </>
        )}
      </section>
      <p className="mt-4 text-xs text-muted-foreground">
        Tìm kiếm, sắp xếp và chia trang áp dụng trên danh sách API trả về. Backend hiện chưa hỗ trợ phân trang phía máy
        chủ.
      </p>
      {form !== null && (
        <EntityFormDialog
          config={config}
          editId={form === "create" ? undefined : form}
          onClose={() => setForm(null)}
          onSaved={() => complete("Đã lưu dữ liệu thành công.")}
        />
      )}
      {deleting && (
        <DeleteDialog
          config={config}
          entity={deleting}
          onClose={() => setDeleting(null)}
          onDeleted={() => complete("Đã xóa dữ liệu thành công.")}
        />
      )}
    </>
  );
}
