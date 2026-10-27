import type { ReactNode } from "react";
import { Search } from "lucide-react";
import { Input } from "@/components/ui/input";
import { enumLabels } from "../constants/enum-labels";
import type { ModuleConfig } from "../types/module-config";

export function EntityListFilters({
  config,
  q,
  status,
  change,
  filters,
}: {
  config: Pick<ModuleConfig, "singular" | "statusOptions">;
  q: string;
  status: string;
  change: (updates: Record<string, string>) => void;
  filters?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-center gap-3 border-t px-5 py-4">
      <div className="relative min-w-48 flex-1 sm:max-w-sm">
        <Search className="absolute left-3 top-2.5 text-muted-foreground" size={16} />
        <Input
          aria-label="Tìm kiếm trong danh sách"
          placeholder={`Tìm ${config.singular}…`}
          value={q}
          onChange={(e) => change({ q: e.target.value })}
          className="pl-9"
        />
      </div>
      {config.statusOptions && (
        <select
          className="h-9 rounded-md border px-3 text-sm"
          aria-label="Lọc trạng thái"
          value={status}
          onChange={(e) => change({ status: e.target.value })}
        >
          <option value="">Tất cả trạng thái</option>
          {config.statusOptions.map((s) => (
            <option key={s} value={s}>
              {enumLabels[s]}
            </option>
          ))}
        </select>
      )}
      {filters}
    </div>
  );
}
