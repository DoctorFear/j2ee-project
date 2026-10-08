// Tính ngày tra cứu chuyến xe

// Lấy ngày địa phương và cộng số ngày cần tra cứu
export function localDate(offset = 0) {
  const date = new Date();
  date.setDate(date.getDate() + offset);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}
