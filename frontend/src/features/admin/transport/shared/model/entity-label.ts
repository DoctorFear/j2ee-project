import type { TransportEntity } from "../types/resource";

export function entityLabel(entity: TransportEntity): string {
  if ("name" in entity)
    return "code" in entity ? `${entity.code} · ${entity.name} (v${entity.versionNo})` : entity.name;
  if ("licensePlate" in entity) return entity.licensePlate;
  if ("fullName" in entity) return entity.fullName;
  if ("code" in entity) return entity.code;
  return `#${"id" in entity ? entity.id : entity.tripId}`;
}
