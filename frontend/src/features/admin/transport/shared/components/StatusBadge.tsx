import { Badge } from "@/components/ui/badge";
import { enumLabels } from "@/features/admin/transport/shared/constants/enum-labels";
export function StatusBadge({ value }: { value: string }) {
  const tone = ["ACTIVE", "OPEN", "COMPLETED"].includes(value)
    ? "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300"
    : ["RUNNING", "SCHEDULED"].includes(value)
      ? "bg-blue-500/10 text-blue-700 dark:text-blue-300"
      : ["CANCELLED", "MAINTENANCE", "PAUSED"].includes(value)
        ? "bg-amber-500/10 text-amber-800 dark:text-amber-300"
        : "bg-muted text-muted-foreground";
  return (
    <Badge variant="secondary" className={`gap-1.5 border-0 font-medium ${tone}`}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {enumLabels[value] ?? value}
    </Badge>
  );
}
