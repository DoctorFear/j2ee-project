import axios, { type InternalAxiosRequestConfig } from "axios";
import { z } from "zod";
import { env } from "@/lib/env";
import { ApiError } from "./error";
import { expireSession, getAccessToken, getRefreshToken, getSessionVersion, replaceTokens } from "./session";

export { ApiError } from "./error";

declare module "axios" {
  interface AxiosRequestConfig {
    authMode?: "none" | "session";
    sessionVersion?: number;
    sentToken?: string | null;
    retried?: boolean;
  }
}

const defaults = { baseURL: env.apiBaseUrl || undefined, timeout: 15000 };
export const http = axios.create(defaults);
const refreshHttp = axios.create(defaults);
const tokensSchema = z.object({ accessToken: z.string().min(1), refreshToken: z.string().min(1) });
let pending: { version: number; promise: Promise<string> } | undefined;

function normalizeError(error: unknown): Error {
  if (!axios.isAxiosError(error)) return error instanceof Error ? error : new ApiError("Yêu cầu không thành công.");
  const status = error.response?.status;
  const body = error.response?.data;
  const validation = body?.errors;
  const messages: unknown[] = Array.isArray(validation)
    ? validation
    : validation && typeof validation === "object"
      ? Object.values(validation).flat()
      : [];
  const message =
    status === 401
      ? "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
      : status === 403
        ? "Bạn không có quyền thực hiện thao tác này."
        : typeof body?.message === "string"
          ? body.message
          : messages.filter((item): item is string => typeof item === "string").join(", ") ||
            (error.code === "ECONNABORTED"
              ? "Máy chủ phản hồi quá chậm. Vui lòng thử lại."
              : !error.response
                ? "Không thể kết nối máy chủ. Vui lòng kiểm tra backend và cấu hình API."
                : `Yêu cầu thất bại (HTTP ${status})`);
  return new ApiError(message, status, /^([A-Za-z]+):/.exec(message)?.[1]);
}

function refreshSession(version: number): Promise<string> {
  if (pending?.version === version) return pending.promise;
  const refreshToken = getRefreshToken();
  const promise = (async () => {
    if (!refreshToken) {
      expireSession(version);
      throw new ApiError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", 401);
    }
    try {
      const response = await refreshHttp.post(
        "/api/auth/refresh-token",
        { refreshToken },
        { baseURL: http.defaults.baseURL }
      );
      if (version !== getSessionVersion()) throw new ApiError("Phiên đăng nhập đã thay đổi. Vui lòng thử lại.", 401);
      const parsed = tokensSchema.safeParse(response.data?.data);
      if (response.data?.success !== true || !parsed.success) {
        expireSession(version);
        throw new ApiError("Phản hồi làm mới phiên không hợp lệ.", 401);
      }
      replaceTokens(parsed.data.accessToken, parsed.data.refreshToken);
      return parsed.data.accessToken;
    } catch (error) {
      if (axios.isAxiosError(error) && [400, 401, 403].includes(error.response?.status ?? 0)) expireSession(version);
      throw normalizeError(error);
    }
  })();
  pending = { version, promise };
  void promise
    .finally(() => {
      if (pending?.promise === promise) pending = undefined;
    })
    .catch(() => {});
  return promise;
}

http.interceptors.request.use((config) => {
  if (config.authMode === "none") return config;
  config.sessionVersion ??= getSessionVersion();
  config.sentToken = getAccessToken();
  if (config.sentToken) config.headers.set("Authorization", `Bearer ${config.sentToken}`);
  return config;
});

async function retryAuthenticated(config: InternalAxiosRequestConfig) {
  const version = config.sessionVersion ?? getSessionVersion();
  if (version !== getSessionVersion()) throw new ApiError("Phiên đăng nhập đã thay đổi. Vui lòng thử lại.", 401);
  if (config.retried) {
    expireSession(version);
    throw new ApiError("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", 401);
  }
  // A parallel request may already have rotated this token.
  if (!getAccessToken() || config.sentToken === getAccessToken()) await refreshSession(version);
  if (version !== getSessionVersion()) throw new ApiError("Phiên đăng nhập đã thay đổi. Vui lòng thử lại.", 401);
  return http.request({ ...config, retried: true });
}

http.interceptors.response.use(
  (response) => {
    if (
      response.status !== 204 &&
      (!response.data || typeof response.data !== "object" || typeof response.data.success !== "boolean")
    ) {
      throw new ApiError("Máy chủ trả về dữ liệu không hợp lệ. Vui lòng kiểm tra cấu hình API.", response.status);
    }
    if (response.data?.success === false)
      throw new ApiError(response.data.message || "Yêu cầu không thành công.", response.status);
    return response;
  },
  async (error: unknown) => {
    if (axios.isCancel(error)) throw error;
    if (
      axios.isAxiosError(error) &&
      error.response?.status === 401 &&
      error.config?.authMode !== "none" &&
      error.config
    )
      return retryAuthenticated(error.config);
    throw normalizeError(error);
  }
);
