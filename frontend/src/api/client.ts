// Gọi API và khai báo endpoint khách hàng
const baseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";
// Gửi yêu cầu JSON và bổ sung token khi có đăng nhập
export async function request<T>(
  path: string,
  options: RequestInit = {},
  accessToken?: string,
): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body) headers.set("Content-Type", "application/json");
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);
  const response = await fetch(`${baseUrl}${path}`, { ...options, headers });
  if (!response.ok) throw new Error(`Yêu cầu thất bại (${response.status})`);
  return response.json() as Promise<T>;
}
// Khai báo đường dẫn API để tích hợp các service
export const customerEndpoints = {
  stations: "/api/stations",
  search: "/api/trips/search",
  seats: (code: string) => `/api/trips/${encodeURIComponent(code)}/seats`,
  hold: "/api/bookings/hold",
  myTickets: "/api/tickets/my",
};
