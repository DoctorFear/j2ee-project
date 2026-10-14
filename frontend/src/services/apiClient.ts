// Core HTTP client cho FutaBus với cơ chế Silent Refresh Token tự động khi gặp lỗi 401
import type { ApiResponse } from "../types/user";

const BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:5001";

const ACCESS_TOKEN_KEY = "futa_access_token";
const REFRESH_TOKEN_KEY = "futa_refresh_token";

export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setTokens(accessToken: string, refreshToken: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

// Biến lưu trữ Promise làm mới token ngầm (singleton để tránh race-condition gọi nhiều lần cùng lúc)
let refreshPromise: Promise<string | null> | null = null;

async function performSilentRefresh(): Promise<string | null> {
  const currentRefreshToken = getRefreshToken();
  if (!currentRefreshToken) {
    clearTokens();
    return null;
  }

  try {
    const res = await fetch(`${BASE_URL}/api/auth/refresh-token`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ refreshToken: currentRefreshToken }),
    });

    if (!res.ok) {
      clearTokens();
      window.dispatchEvent(new Event("futa-auth-expired"));
      return null;
    }

    const json: ApiResponse<{ accessToken: string; refreshToken: string }> = await res.json();
    if (json.success && json.data) {
      setTokens(json.data.accessToken, json.data.refreshToken);
      return json.data.accessToken;
    } else {
      clearTokens();
      window.dispatchEvent(new Event("futa-auth-expired"));
      return null;
    }
  } catch (error) {
    console.error("Lỗi khi tự động làm mới token:", error);
    clearTokens();
    window.dispatchEvent(new Event("futa-auth-expired"));
    return null;
  } finally {
    refreshPromise = null;
  }
}

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {},
  retryOn401 = true,
): Promise<T> {
  const url = endpoint.startsWith("http") ? endpoint : `${BASE_URL}${endpoint}`;
  const headers = new Headers(options.headers || {});

  if (!headers.has("Content-Type") && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  const token = getAccessToken();
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  let response: Response;
  try {
    response = await fetch(url, {
      ...options,
      headers,
    });
  } catch (err) {
    throw new Error("Không thể kết nối đến máy chủ FutaBus. Vui lòng kiểm tra lại kết nối mạng hoặc backend.");
  }

  // Xử lý khi gặp mã 401 Unauthorized -> kích hoạt Silent Refresh Token
  if (response.status === 401 && retryOn401) {
    if (!refreshPromise) {
      refreshPromise = performSilentRefresh();
    }
    const newAccessToken = await refreshPromise;

    if (newAccessToken) {
      // Retry lại request ban đầu với accessToken mới
      const retryHeaders = new Headers(options.headers || {});
      if (!retryHeaders.has("Content-Type") && !(options.body instanceof FormData)) {
        retryHeaders.set("Content-Type", "application/json");
      }
      retryHeaders.set("Authorization", `Bearer ${newAccessToken}`);

      const retryRes = await fetch(url, {
        ...options,
        headers: retryHeaders,
      });

      return handleApiResponse<T>(retryRes);
    }
  }

  return handleApiResponse<T>(response);
}

async function handleApiResponse<T>(response: Response): Promise<T> {
  let responseData: ApiResponse<T> | null = null;
  try {
    responseData = await response.json();
  } catch {
    if (!response.ok) {
      throw new Error(`Yêu cầu thất bại với mã lỗi HTTP ${response.status}`);
    }
    return {} as T;
  }

  if (!response.ok || (responseData && responseData.success === false)) {
    const errorMsg =
      responseData?.message ||
      (responseData?.errors && responseData.errors.length > 0
        ? responseData.errors.join(", ")
        : `Lỗi máy chủ (${response.status})`);
    throw new Error(errorMsg);
  }

  return responseData?.data as T;
}
