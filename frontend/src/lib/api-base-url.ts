export function normalizeApiBaseUrl(value?: string): string {
  const url = value?.trim().replace(/\/+$/, "") ?? "";
  if (!url) return "";
  if (/[\\\s]/.test(url)) throw new Error("VITE_API_BASE_URL chứa ký tự không hợp lệ.");
  if (url.startsWith("/") && !url.startsWith("//") && !/[?#]/.test(url)) return url;
  try {
    const parsed = new URL(url);
    if (
      ["http:", "https:"].includes(parsed.protocol) &&
      !parsed.username &&
      !parsed.password &&
      !parsed.search &&
      !parsed.hash
    )
      return url;
  } catch {
    /* Report one configuration error for all invalid values. */
  }
  throw new Error("VITE_API_BASE_URL phải là URL HTTP(S) hoặc đường dẫn bắt đầu bằng /.");
}
