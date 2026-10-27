import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useEntityDelete } from "@/features/admin/transport/shared/hooks/useEntityDelete";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { TransportEntity } from "@/features/admin/transport/types";
import { entityLabel } from "@/features/admin/transport/shared/model/entity-label";
import type { ModuleConfig } from "@/features/admin/transport/shared/types/module-config";

export function DeleteDialog({
  config,
  entity,
  onClose,
  onDeleted,
}: {
  config: ModuleConfig;
  entity: TransportEntity;
  onClose: () => void;
  onDeleted: () => void;
}) {
  const remove = useEntityDelete(config.api);
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open && !remove.isPending) onClose();
      }}
    >
      <DialogContent className="admin-portal">
        <DialogHeader>
          <DialogTitle>Xóa {config.singular}?</DialogTitle>
          <DialogDescription>
            Bạn đang xóa “{entityLabel(entity)}” (#{entityId(entity)}). Thao tác này không thể hoàn tác.
          </DialogDescription>
        </DialogHeader>
        <p className="text-sm text-muted-foreground">
          Các dữ liệu đang tham chiếu có thể ngăn việc xóa. Máy chủ sẽ kiểm tra ràng buộc trước khi thực hiện.
        </p>
        {remove.error && (
          <p role="alert" className="text-sm text-destructive">
            {remove.error.message}
          </p>
        )}
        <div className="flex justify-end gap-2">
          <Button variant="outline" disabled={remove.isPending} onClick={onClose}>
            Hủy
          </Button>
          <Button
            variant="destructive"
            disabled={remove.isPending}
            onClick={() => remove.mutate(entityId(entity), { onSuccess: onDeleted })}
          >
            {remove.isPending ? "Đang xóa…" : "Xác nhận xóa"}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}
