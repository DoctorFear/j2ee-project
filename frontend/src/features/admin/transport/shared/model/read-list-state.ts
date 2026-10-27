import type { ModuleConfig } from "../types/module-config";
import type { EntityListState } from "../types/entity-list-state";

export function readEntityListState(
  config: Pick<ModuleConfig, "columns" | "defaultSort" | "filterByRoute">,
  params: URLSearchParams
): EntityListState {
  return {
    q: params.get("q") ?? "",
    status: params.get("status") ?? "",
    sortKey: params.get("sort") ?? config.defaultSort ?? config.columns[0],
    desc: params.get("direction") === "desc",
    page: Number(params.get("page")) || 1,
    pageSize: [10, 25, 50].includes(Number(params.get("size"))) ? Number(params.get("size")) : 10,
    routeId: config.filterByRoute ? Number(params.get("routeId")) || undefined : undefined,
  };
}
