const ACCESS_KEY = "futa_access_token";
const REFRESH_KEY = "futa_refresh_token";
const SESSION_KEY = "futa_session_id";
const LOGOUT_KEY = "futa_logged_out_session";
export const AUTH_EXPIRED_EVENT = "futa-auth-expired";
let version = 0;

function getTokenStorage(): Storage {
  return typeof sessionStorage !== "undefined" && sessionStorage.getItem(ACCESS_KEY) ? sessionStorage : localStorage;
}

function removeStoredTokens(): void {
  const stores = [localStorage];
  if (typeof sessionStorage !== "undefined") stores.push(sessionStorage);
  for (const storage of stores) {
    storage.removeItem(ACCESS_KEY);
    storage.removeItem(REFRESH_KEY);
    storage.removeItem(SESSION_KEY);
  }
}

export const getSessionVersion = () => version;
export const getAccessToken = () => getTokenStorage().getItem(ACCESS_KEY);
export const getRefreshToken = () => getTokenStorage().getItem(REFRESH_KEY);

export function setTokens(accessToken: string, refreshToken: string, rememberMe = true): void {
  version += 1;
  removeStoredTokens();
  const storage = rememberMe ? localStorage : sessionStorage;
  storage.setItem(SESSION_KEY, crypto.randomUUID());
  storage.setItem(ACCESS_KEY, accessToken);
  storage.setItem(REFRESH_KEY, refreshToken);
}

export function replaceTokens(accessToken: string, refreshToken: string): void {
  const storage = getTokenStorage();
  storage.setItem(ACCESS_KEY, accessToken);
  storage.setItem(REFRESH_KEY, refreshToken);
}

export function clearTokens(): void {
  version += 1;
  const sessionId = getTokenStorage().getItem(SESSION_KEY);
  removeStoredTokens();
  // Chia sẻ tín hiệu đăng xuất giữa các tab, không lưu token của phiên tạm.
  if (sessionId) localStorage.setItem(LOGOUT_KEY, sessionId);
}

export function subscribeToSessionRemoval(onClear: () => void): () => void {
  const handleStorage = (event: StorageEvent) => {
    if (event.storageArea !== localStorage) return;
    const tokensRemoved =
      (event.key === ACCESS_KEY || event.key === null) && event.newValue === null && getAccessToken() === null;
    const sessionLoggedOut =
      event.key === LOGOUT_KEY && event.newValue !== null && event.newValue === getTokenStorage().getItem(SESSION_KEY);
    if (tokensRemoved || sessionLoggedOut) onClear();
  };
  window.addEventListener("storage", handleStorage);
  return () => window.removeEventListener("storage", handleStorage);
}

export function expireSession(expectedVersion: number): void {
  if (expectedVersion !== version) return;
  clearTokens();
  window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT));
}
