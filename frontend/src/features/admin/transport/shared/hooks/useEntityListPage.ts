import { useSearchParams } from "react-router-dom";
import { useQueries } from "@tanstack/react-query";
import { useEntityList } from "./useEntityList";
import { entityListOptions } from "../queries/list-options";
import { entityLabel } from "../model/entity-label";
import { entityId } from "../model/entity-id";
import { fieldRelation } from "../model/field-relation";
import { readEntityListState } from "../model/read-list-state";
import { selectEntityRows } from "../model/select-entity-rows";
import type { ModuleConfig } from "../types/module-config";
export function useEntityListPage(config: ModuleConfig) {
  const [params, setParams] = useSearchParams();
  const state = readEntityListState(config, params);
  const query = useEntityList(config.api, { routeId: state.routeId });
  const sources = [
    ...new Set(
      config.columns.flatMap((field) => {
        const source = fieldRelation(config, field);
        return source ? [source] : [];
      })
    ),
  ];
  const relations = useQueries({ queries: sources.map((source) => entityListOptions(source)) });
  const relatedText = (field: string, value: unknown) => {
    const source = fieldRelation(config, field);
    const index = source ? sources.indexOf(source) : -1;
    const entity = index >= 0 ? relations[index]?.data?.find((record) => entityId(record) === value) : undefined;
    return entity ? entityLabel(entity) : String(value ?? "");
  };
  const selection = selectEntityRows(query.data ?? [], config.columns, state, relatedText);
  const change = (updates: Record<string, string>, reset = true) => {
    const next = new URLSearchParams(params);
    Object.entries(updates).forEach(([key, value]) => (value ? next.set(key, value) : next.delete(key)));
    if (reset) next.delete("page");
    setParams(next, { replace: true });
  };
  return { ...state, ...selection, query, change };
}
