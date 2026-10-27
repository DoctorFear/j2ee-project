import { Suspense, useEffect } from "react";
import { Outlet } from "react-router-dom";
import { LoadingState } from "@/components/shared/AsyncState";
import { AdminQueryProvider } from "@/app/providers/AdminQueryProvider";
import { useAdminUiStore } from "@/app/stores/admin-ui-store";
import { AdminSidebar } from "./AdminSidebar";
import { AdminHeader } from "./AdminHeader";
import "@/styles/admin.css";

export default function AdminLayout() {
  const collapsed = useAdminUiStore((state) => state.collapsed);
  const mobile = useAdminUiStore((state) => state.mobile);
  const theme = useAdminUiStore((state) => state.theme);

  useEffect(() => {
    document.documentElement.dataset.adminTheme = theme;
    return () => {
      delete document.documentElement.dataset.adminTheme;
    };
  }, [theme]);
  useEffect(() => () => useAdminUiStore.getState().resetNavigation(), []);

  return (
    <AdminQueryProvider>
      <div
        className={`admin-root admin-shell ${collapsed && !mobile ? "is-collapsed" : ""} ${mobile ? "mobile-open" : ""}`}
      >
        <a
          className="sr-only focus:not-sr-only focus:absolute focus:z-50 focus:bg-card focus:p-4"
          href="#admin-content"
        >
          Đến nội dung chính
        </a>
        <AdminSidebar />
        <div className="min-w-0">
          <AdminHeader />
          <main id="admin-content" className="admin-main">
            <Suspense fallback={<LoadingState />}>
              <Outlet />
            </Suspense>
          </main>
        </div>
      </div>
    </AdminQueryProvider>
  );
}
