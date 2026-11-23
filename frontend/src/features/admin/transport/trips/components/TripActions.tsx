import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { Send, Clock, Ban, ShoppingCart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { transportKeys } from "../../shared/queries/keys";
import { tripsApi } from "../api";
import type { Trip } from "../types";

export function TripActions({ trip }: { trip: Trip }) {
  const queryClient = useQueryClient();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Dialog states
  const [saleDialog, setSaleDialog] = useState(false);
  const [newSaleStatus, setNewSaleStatus] = useState<Trip["saleStatus"]>(trip.saleStatus);

  const [estimateDialog, setEstimateDialog] = useState(false);
  const [estDep, setEstDep] = useState(trip.estimatedDepartureAt ?? "");
  const [estArr, setEstArr] = useState(trip.estimatedArrivalAt ?? "");

  const [cancelDialog, setCancelDialog] = useState(false);
  const [cancelReason, setCancelReason] = useState("");

  const refresh = () => queryClient.invalidateQueries({ queryKey: transportKeys.all });

  const handlePublish = async () => {
    if (!confirm("Bạn có chắc chắn muốn phát hành chuyến xe này vào vận hành chính thức?")) return;
    setBusy(true);
    setError(null);
    try {
      await tripsApi.publish(trip.id);
      refresh();
    } catch (e: unknown) {
      alert(e instanceof Error ? e.message : "Không thể phát hành chuyến xe");
    } finally {
      setBusy(false);
    }
  };

  const handleSaveSaleStatus = async () => {
    setBusy(true);
    setError(null);
    try {
      await tripsApi.changeSaleStatus(trip.id, newSaleStatus);
      refresh();
      setSaleDialog(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Không thể đổi trạng thái bán vé");
    } finally {
      setBusy(false);
    }
  };

  const handleSaveEstimate = async () => {
    setBusy(true);
    setError(null);
    try {
      await tripsApi.changeEstimate(trip.id, estDep || null, estArr || null);
      refresh();
      setEstimateDialog(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Không thể cập nhật giờ ước tính");
    } finally {
      setBusy(false);
    }
  };

  const handleCancelTrip = async () => {
    if (!cancelReason.trim()) {
      setError("Vui lòng nhập lý do hủy chuyến");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      await tripsApi.cancel(trip.id, cancelReason.trim());
      refresh();
      setCancelDialog(false);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Không thể hủy chuyến xe");
    } finally {
      setBusy(false);
    }
  };

  const isClosedOrCancelled = trip.status === "COMPLETED" || trip.status === "CANCELLED";

  return (
    <>
      <div className="flex flex-wrap items-center gap-2">
        {trip.status === "DRAFT" && (
          <Button variant="default" size="sm" disabled={busy} onClick={handlePublish}>
            <Send size={14} className="mr-1.5" />
            Phát hành chuyến
          </Button>
        )}

        {!isClosedOrCancelled && (
          <>
            <Button
              variant="outline"
              size="sm"
              disabled={busy}
              onClick={() => {
                setNewSaleStatus(trip.saleStatus);
                setError(null);
                setSaleDialog(true);
              }}
            >
              <ShoppingCart size={14} className="mr-1.5" />
              Đổi trạng thái bán
            </Button>

            <Button
              variant="outline"
              size="sm"
              disabled={busy}
              onClick={() => {
                setEstDep(trip.estimatedDepartureAt ?? "");
                setEstArr(trip.estimatedArrivalAt ?? "");
                setError(null);
                setEstimateDialog(true);
              }}
            >
              <Clock size={14} className="mr-1.5" />
              Cập nhật giờ ước tính
            </Button>

            <Button
              variant="outline"
              size="sm"
              className="text-destructive hover:bg-destructive/10"
              disabled={busy}
              onClick={() => {
                setCancelReason("");
                setError(null);
                setCancelDialog(true);
              }}
            >
              <Ban size={14} className="mr-1.5" />
              Hủy chuyến
            </Button>
          </>
        )}
      </div>

      {/* Dialog Đổi trạng thái bán vé */}
      {saleDialog && (
        <Dialog open onOpenChange={(open) => !open && setSaleDialog(false)}>
          <DialogContent className="admin-portal">
            <DialogHeader>
              <DialogTitle>Trạng thái mở bán vé</DialogTitle>
              <DialogDescription>
                Điều chỉnh trạng thái mở bán vé cho chuyến #{trip.id} ({trip.code}).
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-2">
              <label className="text-sm font-medium">Trạng thái bán vé</label>
              <NativeSelect
                value={newSaleStatus}
                onChange={(e) => setNewSaleStatus(e.target.value as Trip["saleStatus"])}
                className="w-full"
              >
                <NativeSelectOption value="NOT_OPEN">Chưa mở bán (NOT_OPEN)</NativeSelectOption>
                <NativeSelectOption value="OPEN">Mở bán (OPEN)</NativeSelectOption>
                <NativeSelectOption value="PAUSED">Tạm dừng bán (PAUSED)</NativeSelectOption>
                <NativeSelectOption value="CLOSED">Đóng bán (CLOSED)</NativeSelectOption>
              </NativeSelect>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" disabled={busy} onClick={() => setSaleDialog(false)}>
                Hủy
              </Button>
              <Button disabled={busy} onClick={handleSaveSaleStatus}>
                {busy ? "Đang lưu…" : "Cập nhật"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {/* Dialog Cập nhật giờ ước tính */}
      {estimateDialog && (
        <Dialog open onOpenChange={(open) => !open && setEstimateDialog(false)}>
          <DialogContent className="admin-portal">
            <DialogHeader>
              <DialogTitle>Cập nhật giờ ước tính</DialogTitle>
              <DialogDescription>
                Cập nhật giờ khởi hành hoặc giờ đến dự kiến khi chuyến có thay đổi lịch trình.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-2">
              <div>
                <label className="text-sm font-medium">Giờ khởi hành ước tính</label>
                <Input
                  type="datetime-local"
                  step="1"
                  value={estDep}
                  onChange={(e) => setEstDep(e.target.value)}
                  className="mt-1"
                />
              </div>
              <div>
                <label className="text-sm font-medium">Giờ đến ước tính</label>
                <Input
                  type="datetime-local"
                  step="1"
                  value={estArr}
                  onChange={(e) => setEstArr(e.target.value)}
                  className="mt-1"
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" disabled={busy} onClick={() => setEstimateDialog(false)}>
                Hủy
              </Button>
              <Button disabled={busy} onClick={handleSaveEstimate}>
                {busy ? "Đang lưu…" : "Lưu thay đổi"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}

      {/* Dialog Hủy chuyến */}
      {cancelDialog && (
        <Dialog open onOpenChange={(open) => !open && setCancelDialog(false)}>
          <DialogContent className="admin-portal">
            <DialogHeader>
              <DialogTitle>Xác nhận hủy chuyến xe</DialogTitle>
              <DialogDescription>
                Chuyến xe #{trip.id} ({trip.code}) sẽ chuyển sang trạng thái CANCELLED. Thao tác này không thể hoàn tác.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-2">
              <div>
                <label className="text-sm font-medium">
                  Lý do hủy chuyến <span className="text-destructive">*</span>
                </label>
                <Input
                  placeholder="Nhập lý do hủy chuyến (thời tiết, sự cố kỹ thuật,...)"
                  value={cancelReason}
                  onChange={(e) => setCancelReason(e.target.value)}
                  className="mt-1"
                />
              </div>
              {error && <p className="text-sm text-destructive">{error}</p>}
            </div>
            <div className="flex justify-end gap-2">
              <Button variant="outline" disabled={busy} onClick={() => setCancelDialog(false)}>
                Bỏ qua
              </Button>
              <Button variant="destructive" disabled={busy} onClick={handleCancelTrip}>
                {busy ? "Đang xử lý…" : "Xác nhận hủy chuyến"}
              </Button>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </>
  );
}

