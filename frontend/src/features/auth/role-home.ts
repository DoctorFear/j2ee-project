export const getRoleHomePath = (role?: string): string => {
  if (role === "Admin") return "/admin";

  if (role === "Driver") return "/driver/trips";

  return "/";
};

export function getLoginDestination(role: string, returnUrl?: string): string {
  if (!returnUrl?.startsWith("/") || returnUrl.startsWith("//") || returnUrl.includes("\\"))
    return getRoleHomePath(role);

  const path = returnUrl.split(/[?#]/, 1)[0];

  const admin = path === "/admin" || path.startsWith("/admin/");
  const driver = path === "/driver" || path.startsWith("/driver/");

  if (role === "Admin") return admin ? returnUrl : getRoleHomePath(role);
  if (role === "Driver") return driver ? returnUrl : getRoleHomePath(role);

  return admin || driver ? "/" : returnUrl;
}
