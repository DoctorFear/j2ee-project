import { normalizeApiBaseUrl } from "@/lib/api-base-url";

export const env = {
  apiBaseUrl: normalizeApiBaseUrl(import.meta.env.VITE_API_BASE_URL),
  googleClientId: import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim() ?? "",
};
