import { AlertCircle, Inbox, LoaderCircle } from "lucide-react";
import { Button } from "@/components/ui/button";
export function LoadingState() {
  return (
    <div role="status" className="flex min-h-36 items-center justify-center gap-2 text-muted-foreground">
      <LoaderCircle className="animate-spin" size={18} />
      Đang tải dữ liệu…
    </div>
  );
}
export function ErrorState({ error, retry }: { error: Error; retry?: () => void }) {
  return (
    <div role="alert" className="flex min-h-36 flex-col items-center justify-center gap-3 p-6 text-center">
      <AlertCircle className="text-destructive" size={25} />
      <p className="max-w-lg text-sm">{error.message}</p>
      {retry && (
        <Button variant="outline" onClick={retry}>
          Thử lại
        </Button>
      )}
    </div>
  );
}
export function EmptyState({
  message = "Chưa có dữ liệu",
  detail = "Thêm bản ghi mới hoặc thay đổi bộ lọc để bắt đầu.",
}: {
  message?: string;
  detail?: string;
}) {
  return (
    <div className="flex min-h-48 flex-col items-center justify-center gap-2 p-6 text-center">
      <Inbox size={30} className="mb-2 text-muted-foreground" />
      <strong>{message}</strong>
      <p className="text-sm text-muted-foreground">{detail}</p>
    </div>
  );
}
