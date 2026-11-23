import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { transportKeys } from "../../shared/queries/keys";
import { vehiclesApi } from "../api";
import type { Vehicle } from "../types";

export function VehicleStatusAction({ vehicle }: { vehicle: Vehicle }) {
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [status, setStatus] = useState<Vehicle["status"]>(vehicle.status);
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    setBusy(true);
    setError(null);
    try {
      await vehiclesApi.updateStatus(vehicle.id, status);
      queryClient.invalidateQueries({ queryKey: transportKeys.all });
      setOpen(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Không thể cập nhật trạng thái phương tiện");
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <Button
        variant="outline"
        size="sm"
        disabled={busy}
        onClick={() => {
          setStatus(vehicle.status);
          setError(null);
          setOpen(true);
        }}
      >
        <RefreshCw size={14} className="mr-1.5" />
        Đổi trạng thái
      </Button>

      {open && (
        <Dialog open onOpenChange={(v) => !v && setOpen(false)}>
          <DialogContent className="admin-portal">
            <DialogHeader>
              <DialogTitle>Đổi trạng thái phương tiện</DialogTitle>
              <DialogDescription>
                Cập nhật trạng thái khai thác cho xe biển số {vehicle.licensePlate} (#{vehicle.id}).
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-2">
              <label className="text-sm font-medium">Trạng thái mới</label>
              <NativeSelect
                value={status}
                onChange={(e) => setStatus(e.target.value as Vehicle["status"])}
                className="w-full"
              >
                <NativeSelectOption value="ACTIVE">Hoạt động (ACTIVE)</NativeSelectOption>
                <NativeSelectOption value="MAINTENANCE">Bảo trì (MAINTENANCE)</NativeSelectOption>
                <NativeSelectOption value="INACTIVE">Ngừng hoạt động (INACTIVE)</NativeSelectOption>
              </NativeSelect>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" disabled={busy} onClick={() => setOpen(false)}>
                Hủy
              </Button>
              <Button disabled={busy} onClick={handleSave}>
                {busy ? "Đang lưu…" : "Cập nhật"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </>
  );
}

