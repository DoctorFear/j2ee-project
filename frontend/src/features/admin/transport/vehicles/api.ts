import { createResourceApi } from "../shared/api/create-resource-api";
import { apiClient } from "@/api/client";
import type { Vehicle } from "./types";

const baseApi = createResourceApi("vehicles");

export const vehiclesApi = {
  ...baseApi,
  updateStatus: (id: number, status: Vehicle["status"]) =>
    apiClient<Vehicle>(`/vehicles/${id}/status`, { method: "PUT", data: { status } }),
};
