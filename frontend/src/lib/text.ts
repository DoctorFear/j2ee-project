// Chuẩn hóa từ khóa tìm kiếm tiếng Việt

// Bỏ dấu và chuyển chữ thường để tìm kiếm bến xe
export const normalize = (s: string) =>
  s
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replaceAll("đ", "d")
    .toLowerCase();
