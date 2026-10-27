import { apiClient } from "@/api/client";
import type { Station } from "./types";
import type { FormValues } from "../shared/types/form";
import { createResourceApi } from "../shared/api/create-resource-api";

export const stationsApi = {
  ...createResourceApi("stations"),
  update: (id: number, values: FormValues) =>
    apiClient<Station>("/api/stations/" + id, { method: "PUT", data: values }),
};
