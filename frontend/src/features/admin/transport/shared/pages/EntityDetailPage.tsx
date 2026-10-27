import { PageHeading } from "@/components/shared/PageHeading";
import { useState, type ReactNode } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { LoadingState, ErrorState } from "@/components/shared/AsyncState";
import { useEntityDetail } from "@/features/admin/transport/shared/hooks/useEntityDetail";
import type { ModuleConfig } from "../types/module-config";
import { fieldRelation } from "../model/field-relation";
import { entityLabel } from "@/features/admin/transport/shared/model/entity-label";
import { labels } from "@/features/admin/transport/shared/constants/field-labels";
import { EntityValue } from "@/features/admin/transport/shared/components/EntityValue";
import { EntityFormDialog } from "@/features/admin/transport/shared/components/EntityFormDialog";
import { DeleteDialog } from "@/features/admin/transport/shared/components/DeleteDialog";

export function EntityDetailPage({ config, children }: { config: ModuleConfig; children?: (id: number) => ReactNode }) {
  const id = Number(useParams().id);
  const query = useEntityDetail(config.api, id);
  const navigate = useNavigate();
  const [edit, setEdit] = useState(false);
  const [remove, setRemove] = useState(false);
  const [notice, setNotice] = useState("");
  if (!Number.isSafeInteger(id) || id <= 0) return <ErrorState error={new Error("Mã bản ghi không hợp lệ.")} />;
  if (query.isPending) return <LoadingState />;
  if (query.isError) return <ErrorState error={query.error} retry={() => void query.refetch()} />;
  const entity = query.data;
  return (
    <>
      <Button variant="ghost" asChild className="mb-4">
        <Link to={`/admin/${config.resource}`}>
          <ArrowLeft size={15} />
          Danh sách {config.singular}
        </Link>
      </Button>
      <PageHeading
        className="admin-page-heading"
        title={entityLabel(entity)}
        description={
          <>
            {config.title} / #{id}
          </>
        }
        actions={
          <div className="flex gap-2">
            {config.updateSchema && (
              <Button variant="outline" onClick={() => setEdit(true)}>
                <Pencil size={15} />
                Chỉnh sửa
              </Button>
            )}
            <Button variant="outline" className="text-destructive" onClick={() => setRemove(true)}>
              <Trash2 size={15} />
              Xóa {config.singular}
            </Button>
          </div>
        }
      />
      {notice && (
        <p role="status" className="mb-4 text-emerald-600">
          {notice}
        </p>
      )}
      <section className="admin-panel">
        <div className="admin-panel-heading">
          <h2>Thông tin chi tiết</h2>
        </div>
        <dl className="m-0 grid gap-x-8 px-6 pb-6 sm:grid-cols-2">
          {Object.entries(entity).map(([key, value]) => (
            <div key={key} className="border-t py-4">
              <dt className="mb-1 text-xs text-muted-foreground">{labels[key] ?? key}</dt>
              <dd className="m-0 break-words text-sm font-medium">
                <EntityValue field={key} value={value} relation={fieldRelation(config, key)} />
              </dd>
            </div>
          ))}
        </dl>
      </section>
      {children?.(id)}
      {edit && (
        <EntityFormDialog
          config={config}
          editId={id}
          onClose={() => setEdit(false)}
          onSaved={() => {
            setEdit(false);
            setNotice("Đã cập nhật dữ liệu.");
          }}
        />
      )}
      {remove && (
        <DeleteDialog
          config={config}
          entity={entity}
          onClose={() => setRemove(false)}
          onDeleted={() => navigate(`/admin/${config.resource}`)}
        />
      )}
    </>
  );
}
