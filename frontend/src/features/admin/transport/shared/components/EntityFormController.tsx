import type { ModuleConfig } from "../types/module-config";
import type { TransportEntity } from "../types/resource";
import { useEntityForm } from "../hooks/useEntityForm";
import { EntityForm } from "./EntityForm";
import { EntityOptions } from "./EntityOptions";

export function EntityFormController({
  config,
  entity,
  onSaved,
  onCancel,
  setBusy,
}: {
  config: ModuleConfig;
  entity?: TransportEntity;
  onSaved: () => void;
  onCancel: () => void;
  setBusy: (busy: boolean) => void;
}) {
  const form = useEntityForm({ config, entity, onSaved, setBusy });
  return (
    <EntityForm
      form={form}
      note={config.note}
      onCancel={onCancel}
      renderRelationOptions={(api) => <EntityOptions api={api} />}
    />
  );
}
