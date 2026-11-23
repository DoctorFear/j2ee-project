import { createResourceApi } from "../shared/api/create-resource-api";
import { apiClient } from "@/api/client";
import type { TripStatistic } from "./types";
import type { FormValues } from "../shared/types/form";

const baseApi = createResourceApi("trip-statistics");

export const tripStatisticsApi = {
  ...baseApi,
  create: (values: FormValues) =>
    apiClient<TripStatistic>("/trip-statistics", {
      method: "POST",
      data: {
        ...values,
        sourceRevision: values.sourceRevision ?? 1,
        sourceAsOfAt: values.sourceAsOfAt ?? new Date().toISOString().slice(0, 19),
      },
    }),
};
