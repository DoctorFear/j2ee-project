import { EntityFormController } from "./EntityFormController";
import { useState } from "react";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { LoadingState, ErrorState } from "@/components/shared/AsyncState";
import { useEntityDetail } from "@/features/admin/transport/shared/hooks/useEntityDetail";
import type { ModuleConfig } from "@/features/admin/transport/shared/types/module-config";

export function EntityFormDialog({
  config,
  editId,
  onClose,
  onSaved,
}: {
  config: ModuleConfig;
  editId?: number;
  onClose: () => void;
  onSaved: () => void;
}) {
  const detail = useEntityDetail(config.api, editId ?? 0);
  const [busy, setBusy] = useState(false);
  return (
    <Dialog
      open
      onOpenChange={(open) => {
        if (!open && !busy) onClose();
      }}
    >
      <DialogContent className="admin-portal max-h-[90dvh] overflow-y-auto sm:max-w-2xl">
        <DialogHeader>
          <DialogTitle>
            {editId ? "Chỉnh sửa" : "Thêm"} {config.singular}
          </DialogTitle>
          <DialogDescription>{config.description}</DialogDescription>
        </DialogHeader>
        {editId && detail.isPending ? (
          <LoadingState />
        ) : editId && detail.isError ? (
          <ErrorState error={detail.error} retry={() => void detail.refetch()} />
        ) : (
          <EntityFormController
            config={config}
            entity={editId ? detail.data : undefined}
            setBusy={setBusy}
            onCancel={onClose}
            onSaved={onSaved}
          />
        )}
      </DialogContent>
    </Dialog>
  );
}
