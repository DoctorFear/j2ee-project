export function ApplicationErrorPage() {
  return (
    <main className="fallback" role="alert">
      <h1>Không thể hiển thị trang</h1>
      <p>Vui lòng tải lại trang để thử lại.</p>
      <button type="button" onClick={() => window.location.reload()}>
        Tải lại trang
      </button>
    </main>
  );
}
