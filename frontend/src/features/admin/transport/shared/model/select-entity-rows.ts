import { enumLabels } from "../constants/enum-labels";
import { readField } from "./read-field";
import type { EntityListState } from "../types/entity-list-state";
import type { TransportEntity } from "../types/resource";

export function selectEntityRows<T extends TransportEntity>(
  records: readonly T[],
  columns: readonly string[],
  state: EntityListState,
  resolveText: (field: string, value: unknown) => string
) {
  const search = state.q.toLocaleLowerCase("vi");
  const filtered = records.filter(
    (row) =>
      (!state.status || readField(row, "status") === state.status) &&
      columns.some((field) => {
        const value = readField(row, field);
        return `${resolveText(field, value)} ${enumLabels[String(value)] ?? ""}`
          .toLocaleLowerCase("vi")
          .includes(search);
      })
  );
  const sorted = [...filtered].sort((a, b) => {
    const av = readField(a, state.sortKey);
    const bv = readField(b, state.sortKey);
    const result =
      typeof av === "number" && typeof bv === "number"
        ? av - bv
        : resolveText(state.sortKey, av).localeCompare(resolveText(state.sortKey, bv), "vi", {
            numeric: true,
          });
    return state.desc ? -result : result;
  });
  const page = Math.min(Math.max(1, state.page), Math.max(1, Math.ceil(sorted.length / state.pageSize)));
  return {
    rows: sorted.slice((page - 1) * state.pageSize, page * state.pageSize),
    total: sorted.length,
    page,
  };
}
