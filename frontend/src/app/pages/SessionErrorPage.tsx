export function SessionErrorPage({
  error,
  loading,
  onRetry,
}: {
  error: string;
  loading: boolean;
  onRetry: () => void;
}) {
  return (
    <main className="fallback" role="alert">
      <h1>Không thể tải thông tin tài khoản</h1>
      <p>{error}</p>
      <button type="button" disabled={loading} onClick={onRetry}>
        Thử lại
      </button>
    </main>
  );
}
