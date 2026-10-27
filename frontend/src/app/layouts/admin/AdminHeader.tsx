import { Link, useLocation } from "react-router-dom";
import { ChevronRight, ChevronsLeft, Menu, Moon, Sun } from "lucide-react";
import { Button } from "@/components/ui/button";

import { transportTitles } from "@/features/admin/transport/navigation";
import type { Resource } from "@/features/admin/transport/types";
import { useAdminUiStore } from "@/app/stores/admin-ui-store";

export function AdminHeader() {
  const collapsed = useAdminUiStore((state) => state.collapsed);
  const mobile = useAdminUiStore((state) => state.mobile);
  const theme = useAdminUiStore((state) => state.theme);
  const toggleTheme = useAdminUiStore((state) => state.toggleTheme);
  const toggleNavigation = useAdminUiStore((state) => state.toggleNavigation);
  const dark = theme === "dark";
  const location = useLocation();
  const segment = location.pathname.split("/")[2];
  const resource = segment && segment in transportTitles ? (segment as Resource) : undefined;
  const title = resource ? transportTitles[resource] : segment === "account" ? "Thông tin tài khoản" : "Tổng quan";

  return (
    <header className="admin-topbar">
      <div className="flex min-w-0 items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          aria-label="Thu gọn hoặc mở menu"
          aria-controls="admin-navigation"
          aria-expanded={mobile || !collapsed}
          onClick={() => {
            toggleNavigation(window.matchMedia("(max-width: 760px)").matches);
          }}
        >
          {collapsed ? <Menu size={19} /> : <ChevronsLeft size={19} />}
        </Button>
        <div className="flex min-w-0 items-center gap-2 text-xs text-muted-foreground">
          <Link to="/admin" className="hidden sm:inline">
            Không gian quản trị
          </Link>
          <ChevronRight size={13} className="hidden sm:inline" />
          <span className="truncate font-medium text-foreground">{title}</span>
          {location.pathname.split("/")[3] && (
            <>
              <ChevronRight size={13} />
              <span>Chi tiết</span>
            </>
          )}
        </div>
      </div>
      <div className="flex items-center gap-3">
        <span className="hidden text-xs text-muted-foreground lg:inline">
          {new Intl.DateTimeFormat("vi-VN", {
            weekday: "long",
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            timeZone: "Asia/Ho_Chi_Minh",
          }).format(new Date())}
        </span>
        <Button
          variant="outline"
          size="icon"
          aria-label={dark ? "Bật giao diện sáng" : "Bật giao diện tối"}
          onClick={toggleTheme}
        >
          {dark ? <Sun size={16} /> : <Moon size={16} />}
        </Button>
        <Link
          to="/admin/account"
          className="admin-avatar"
          aria-label="Xem thông tin tài khoản"
          title="Thông tin tài khoản"
        >
          AD
        </Link>
      </div>
    </header>
  );
}
