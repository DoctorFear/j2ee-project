import type { AxiosRequestConfig } from "axios";
import { http } from "./http";
import type { ApiEnvelope } from "./types";

export type { ApiEnvelope } from "./types";

export async function apiClient<T>(endpoint: string, options: AxiosRequestConfig = {}): Promise<T> {
  const response = await http.request<ApiEnvelope<T>>({ ...options, url: endpoint });
  return response.status === 204 ? (undefined as T) : response.data.data;
}
