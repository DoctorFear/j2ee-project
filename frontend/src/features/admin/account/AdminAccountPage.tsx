import { useQuery } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";
import { sessionApi, useAuthStore } from "@/features/auth";
import { LoadingState, ErrorState } from "@/components/shared/AsyncState";
import { Button } from "@/components/ui/button";
import { PageHeading } from "@/components/shared/PageHeading";
import { AccountDetails } from "./components/AccountDetails";
import styles from "./AdminAccount.module.css";

export default function AdminAccountPage() {
  const userId = useAuthStore((state) => state.user?.id);
  const profile = useQuery({
    queryKey: ["account", "profile", userId],
    queryFn: () => sessionApi.getProfile(),
    enabled: userId !== undefined,
    staleTime: 60000,
  });

  return (
    <section aria-labelledby="admin-account-heading" aria-busy={profile.isFetching}>
      <PageHeading
        className={`admin-page-heading ${styles.heading}`}
        titleId="admin-account-heading"
        title="Thông tin tài khoản"
        description="Xem thông tin tài khoản quản trị đang đăng nhập."
        actions={
          <Button variant="outline" disabled={profile.isFetching} onClick={() => void profile.refetch()}>
            <RefreshCw size={16} className={profile.isFetching ? "animate-spin" : undefined} />
            Làm mới thông tin
          </Button>
        }
      />
      {profile.isPending ? (
        <LoadingState />
      ) : profile.isError ? (
        <ErrorState error={profile.error} retry={() => void profile.refetch()} />
      ) : (
        <AccountDetails profile={profile.data} />
      )}
    </section>
  );
}
