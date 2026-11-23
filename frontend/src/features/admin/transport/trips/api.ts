import { createResourceApi } from "../shared/api/create-resource-api";
import { apiClient } from "@/api/client";
import type { Trip } from "./types";

const baseApi = createResourceApi("trips");

export const tripsApi = {
  ...baseApi,
  publish: (id: number) =>
    apiClient<Trip>(`/trips/${id}/publish`, { method: "POST" }),
  changeSaleStatus: (id: number, saleStatus: Trip["saleStatus"]) =>
    apiClient<Trip>(`/trips/${id}/sale-status`, { method: "PUT", data: { saleStatus } }),
  changeEstimate: (id: number, estimatedDepartureAt?: string | null, estimatedArrivalAt?: string | null) =>
    apiClient<Trip>(`/trips/${id}/estimate`, {
      method: "PUT",
      data: {
        estimatedDepartureAt: estimatedDepartureAt || null,
        estimatedArrivalAt: estimatedArrivalAt || null,
      },
    }),
  cancel: (id: number, reason: string) =>
    apiClient<Trip>(`/trips/${id}/cancel`, { method: "POST", data: { reason } }),
};
