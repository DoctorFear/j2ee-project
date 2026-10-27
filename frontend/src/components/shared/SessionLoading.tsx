import styles from "@/components/shared/SessionLoading.module.css";

export function SessionLoading() {
  return (
    <div className={styles.container} role="status">
      <div className={styles.spinner} aria-hidden="true" />
      <p className={styles.message}>Đang xác thực thông tin tài khoản...</p>
    </div>
  );
}
