// Lưu và khôi phục đường dẫn đặt vé sau khi đăng nhập
type BookingSelection = {
  code?: string;
  path?: string;
  origin?: string;
  destination?: string;
};

// Đọc lựa chọn đã lưu và loại bỏ dữ liệu không đúng định dạng
export function readBookingSelection(): BookingSelection | null {
  try {
    const value = JSON.parse(
      sessionStorage.getItem("futa-booking-selection") || "null",
    );
    return value && typeof value === "object" ? value : null;
  } catch {
    sessionStorage.removeItem("futa-booking-selection");
    return null;
  }
}

// Chỉ khôi phục đường dẫn đặt vé nội bộ của ứng dụng
export function bookingReturnPath(
  value: BookingSelection | null,
): string | null {
  if (!value?.code) return null;
  if (value.path?.startsWith("/trips/") && !value.path.includes("\\"))
    return value.path;
  return `/trips/${encodeURIComponent(value.code)}/book?origin=${encodeURIComponent(value.origin || "")}&destination=${encodeURIComponent(value.destination || "")}`;
}
