import type { Resource } from "../types/resource";

export const transportKeys = {
  all: ["transport"] as const,
  list: (resource: Resource, routeId?: number) => ["transport", resource, "list", routeId ?? null] as const,
  detail: (resource: Resource, id: number) => ["transport", resource, "detail", id] as const,
};
