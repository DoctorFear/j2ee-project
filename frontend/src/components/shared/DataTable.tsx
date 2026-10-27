import type { ReactNode } from "react";
import { ArrowDown, ArrowUp, ArrowUpDown } from "lucide-react";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { EmptyState } from "@/components/shared/AsyncState";
export interface Column<T> {
  key: string;
  label: string;
  render: (row: T) => ReactNode;
}
export function DataTable<T>({
  rows,
  columns,
  rowKey,
  actions,
  sort,
  onSort,
  caption,
}: {
  rows: T[];
  columns: Column<T>[];
  rowKey: (row: T) => number;
  actions?: (row: T) => ReactNode;
  sort?: { key: string; desc: boolean };
  onSort?: (key: string) => void;
  caption: string;
}) {
  if (!rows.length) return <EmptyState />;
  return (
    <Table>
      <caption className="sr-only">{caption}</caption>
      <TableHeader>
        <TableRow className="bg-muted/60 hover:bg-muted/60">
          {columns.map((col) => (
            <TableHead
              key={col.key}
              aria-sort={sort?.key === col.key ? (sort.desc ? "descending" : "ascending") : undefined}
              className="h-11 whitespace-nowrap px-5 text-xs font-medium text-muted-foreground"
            >
              {onSort ? (
                <button
                  type="button"
                  onClick={() => onSort(col.key)}
                  className="inline-flex items-center gap-1.5 border-0 bg-transparent p-0 font-medium text-inherit"
                >
                  {col.label}
                  {sort?.key === col.key ? (
                    sort.desc ? (
                      <ArrowDown size={12} />
                    ) : (
                      <ArrowUp size={12} />
                    )
                  ) : (
                    <ArrowUpDown size={12} />
                  )}
                </button>
              ) : (
                col.label
              )}
            </TableHead>
          ))}
          {actions && <TableHead className="px-5 text-right text-xs">Thao tác</TableHead>}
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row) => (
          <TableRow key={rowKey(row)}>
            {columns.map((col) => (
              <TableCell key={col.key} className="max-w-72 px-5 py-4 text-sm">
                <div className="line-clamp-2">{col.render(row)}</div>
              </TableCell>
            ))}
            {actions && (
              <TableCell className="px-5 text-right">
                <div className="flex justify-end gap-1">{actions(row)}</div>
              </TableCell>
            )}
          </TableRow>
        ))}
      </TableBody>
    </Table>
  );
}
export function TablePagination({
  page,
  pageSize,
  total,
  onPage,
  onPageSize,
}: {
  page: number;
  pageSize: number;
  total: number;
  onPage: (page: number) => void;
  onPageSize: (size: number) => void;
}) {
  const pages = Math.max(1, Math.ceil(total / pageSize));
  return (
    <div className="flex flex-wrap items-center justify-between gap-3 border-t px-5 py-4 text-xs text-muted-foreground">
      <span>
        {total ? `${(page - 1) * pageSize + 1}–${Math.min(page * pageSize, total)}` : "0"} / {total} bản ghi
      </span>
      <div className="flex items-center gap-2">
        <label htmlFor="page-size">Số dòng</label>
        <select
          id="page-size"
          value={pageSize}
          onChange={(e) => onPageSize(Number(e.target.value))}
          className="rounded border px-2 py-1"
        >
          {[10, 25, 50].map((size) => (
            <option key={size}>{size}</option>
          ))}
        </select>
        <Button variant="outline" size="sm" disabled={page <= 1} onClick={() => onPage(page - 1)}>
          Trước
        </Button>
        <span>
          {page} / {pages}
        </span>
        <Button variant="outline" size="sm" disabled={page >= pages} onClick={() => onPage(page + 1)}>
          Sau
        </Button>
      </div>
    </div>
  );
}
