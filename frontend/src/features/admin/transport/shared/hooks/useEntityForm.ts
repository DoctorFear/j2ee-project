import { useForm } from "react-hook-form";
import { formResolver } from "../forms/form-resolver";
import { ApiError } from "@/api/http";
import { useEntitySave } from "@/features/admin/transport/shared/hooks/useEntitySave";
import type { FieldConfig } from "@/features/admin/transport/shared/types/field-config";
import type { ModuleConfig } from "@/features/admin/transport/shared/types/module-config";
import { entityId } from "@/features/admin/transport/shared/model/entity-id";
import type { FormValues, TransportEntity } from "@/features/admin/transport/types";

export function useEntityForm({
  config,
  entity,
  onSaved,
  setBusy,
}: {
  config: ModuleConfig;
  entity?: TransportEntity;
  onSaved: () => void;
  setBusy: (busy: boolean) => void;
}) {
  const edit = entity !== undefined;
  const fields = edit ? (config.updateFields ?? config.fields) : config.fields;
  const initial = entity
    ? Object.fromEntries(fields.map((field) => [field.name, (entity as unknown as FormValues)[field.name] ?? null]))
    : config.defaults;
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    defaultValues: initial,
    resolver: formResolver(edit ? config.updateSchema! : config.schema),
  });
  const save = useEntitySave(config.api, entity ? entityId(entity) : undefined);
  const registerField = (field: FieldConfig) =>
    register(field.name, {
      setValueAs: (value) =>
        field.optional && value === ""
          ? null
          : field.kind === "number" || field.relation
            ? value === ""
              ? undefined
              : Number(value)
            : value,
    });
  const submit = handleSubmit(async (values) => {
    setBusy(true);
    try {
      await save.mutateAsync(values);
      onSaved();
    } catch (error) {
      if (error instanceof ApiError && error.field && fields.some((f) => f.name === error.field))
        setError(error.field, { message: error.message });
    } finally {
      setBusy(false);
    }
  });
  return { edit, fields, registerField, errors, isSubmitting, submit, saveError: save.error };
}

export type EntityFormState = ReturnType<typeof useEntityForm>;
