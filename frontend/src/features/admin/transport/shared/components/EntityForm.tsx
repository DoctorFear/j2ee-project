import { EntityFormField } from "./EntityFormField";
import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import type { EntityFormState } from "@/features/admin/transport/shared/hooks/useEntityForm";
import type { EntityApi } from "../types/entity-api";

export function EntityForm({
  form,
  note,
  onCancel,
  renderRelationOptions,
}: {
  form: EntityFormState;
  note?: string;
  onCancel: () => void;
  renderRelationOptions: (api: EntityApi) => ReactNode;
}) {
  const { edit, fields, isSubmitting, submit, saveError } = form;
  return (
    <form onSubmit={submit} noValidate className="space-y-5">
      <div className="grid gap-4 sm:grid-cols-2">
        {fields.map((field) => (
          <EntityFormField key={field.name} field={field} form={form} renderRelationOptions={renderRelationOptions} />
        ))}
      </div>
      {note && <p className="rounded-md bg-muted p-3 text-xs text-muted-foreground">{note}</p>}
      {saveError && (
        <p role="alert" className="text-sm text-destructive">
          {saveError.message}
        </p>
      )}
      <div className="flex justify-end gap-2 border-t pt-4">
        <Button variant="outline" type="button" disabled={isSubmitting} onClick={onCancel}>
          Hủy
        </Button>
        <Button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Đang lưu…" : edit ? "Lưu thay đổi" : "Tạo mới"}
        </Button>
      </div>
    </form>
  );
}
