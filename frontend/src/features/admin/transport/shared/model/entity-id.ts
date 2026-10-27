import type { TransportEntity } from "../types/resource";

export function entityId(entity: TransportEntity): number {
  return "id" in entity ? entity.id : entity.tripId;
}
