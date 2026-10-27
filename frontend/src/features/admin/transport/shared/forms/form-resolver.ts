import type { Resolver } from "react-hook-form";
import type { z } from "zod";
import type { FormValues } from "../types/form";

export function formResolver(schema: z.ZodType<FormValues>): Resolver<FormValues> {
  return async (values) => {
    const parsed = schema.safeParse(values);
    if (parsed.success) return { values: parsed.data, errors: {} };
    return {
      values: {},
      errors: Object.fromEntries(
        parsed.error.issues.map((issue) => [String(issue.path[0]), { type: "validate", message: issue.message }])
      ),
    };
  };
}
