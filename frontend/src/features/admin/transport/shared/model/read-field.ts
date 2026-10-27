import type { TransportEntity } from "../types/resource";

export function readField(entity: TransportEntity, field: string): unknown {
  return (entity as unknown as Record<string, unknown>)[field];
}
