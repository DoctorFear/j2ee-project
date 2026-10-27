import { Link } from "react-router-dom";
import { Eye, Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { entityLabel } from "../model/entity-label";
import { entityId } from "../model/entity-id";
import type { ModuleConfig } from "../types/module-config";
import type { TransportEntity } from "../types/resource";

export function EntityRowActions({
  config,
  row,
  onEdit,
  onDelete,
}: {
  config: ModuleConfig;
  row: TransportEntity;
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <>
      <Button size="icon" variant="ghost" asChild>
        <Link aria-label={`Chi tiết ${entityLabel(row)}`} to={`/admin/${config.resource}/${entityId(row)}`}>
          <Eye size={15} />
        </Link>
      </Button>
      {config.updateSchema && (
        <Button size="icon" variant="ghost" aria-label={`Sửa ${entityLabel(row)}`} onClick={() => onEdit()}>
          <Pencil size={15} />
        </Button>
      )}
      <Button
        size="icon"
        variant="ghost"
        className="text-destructive"
        aria-label={`Xóa ${entityLabel(row)}`}
        onClick={() => onDelete()}
      >
        <Trash2 size={15} />
      </Button>
    </>
  );
}
