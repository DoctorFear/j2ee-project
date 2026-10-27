import { Link } from "react-router-dom";
import styles from "./NotFoundPage.module.css";

export default function NotFoundPage() {
  return (
    <main className={`fallback ${styles.page}`}>
      <h1 className={styles.title}>Trang chưa được triển khai</h1>
      <p className={styles.description}>
        Đường dẫn bạn yêu cầu hiện đang được phát triển bởi các thành viên trong nhóm.
      </p>
      <Link to="/" className={styles.homeLink}>
        Về trang chủ tìm chuyến
      </Link>
    </main>
  );
}
