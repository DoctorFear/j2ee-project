import type { ReactNode } from "react";
import { Input } from "@/components/ui/input";
import { NativeSelect, NativeSelectOption } from "@/components/ui/native-select";
import { enumLabels } from "@/features/admin/transport/shared/constants/enum-labels";
import { labels } from "@/features/admin/transport/shared/constants/field-labels";
import type { EntityFormState } from "@/features/admin/transport/shared/hooks/useEntityForm";
import type { EntityApi } from "../types/entity-api";

import type { FieldConfig } from "../types/field-config";

export function EntityFormField({
  field,
  form,
  renderRelationOptions,
}: {
  field: FieldConfig;
  form: EntityFormState;
  renderRelationOptions: (api: EntityApi) => ReactNode;
}) {
  const { registerField, errors } = form;

  const fieldId = `field-${field.name}`;
  const errorId = `${fieldId}-error`;
  const registration = registerField(field);
  return (
    <div key={field.name} className={`admin-field ${field.name === "address" ? "sm:col-span-2" : ""}`}>
      <label htmlFor={fieldId}>
        {field.label ?? labels[field.name]}
        {field.optional ? (
          <span className="ml-1 text-xs font-normal text-muted-foreground">(tùy chọn)</span>
        ) : (
          <span className="ml-1 text-destructive">*</span>
        )}
      </label>
      {field.kind === "select" ? (
        <NativeSelect
          id={fieldId}
          {...registration}
          aria-invalid={!!errors[field.name]}
          aria-describedby={errors[field.name] ? errorId : undefined}
          className="w-full"
        >
          <NativeSelectOption value="">Chọn {labels[field.name]?.toLowerCase()}</NativeSelectOption>
          {field.relation
            ? renderRelationOptions(field.relation)
            : field.options?.map((option) => (
                <NativeSelectOption key={option} value={option}>
                  {enumLabels[option] ?? option}
                </NativeSelectOption>
              ))}
        </NativeSelect>
      ) : field.kind === "checkbox" ? (
        <input id={fieldId} type="checkbox" {...registration} className="h-5 w-5 accent-orange-600" />
      ) : (
        <Input
          id={fieldId}
          type={field.kind ?? "text"}
          step={field.kind === "datetime-local" ? "1" : field.step}
          {...registration}
          aria-invalid={!!errors[field.name]}
          aria-describedby={errors[field.name] ? errorId : undefined}
          autoComplete="off"
        />
      )}
      {errors[field.name] && (
        <span id={errorId} role="alert" className="text-xs text-destructive">
          {errors[field.name]?.message}
        </span>
      )}
    </div>
  );
}
