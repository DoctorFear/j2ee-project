import { z } from "zod";

export const text = z
  .string()
  .max(255, "Tối đa 255 ký tự")
  .refine((v) => v.trim().length > 0, "Vui lòng nhập thông tin");
export const optionalText = z.string().max(255, "Tối đa 255 ký tự").nullable();
export const int = z
  .number({ error: "Vui lòng nhập số" })
  .int("Phải là số nguyên")
  .min(0, "Không được âm")
  .max(2147483647, "Vượt giới hạn số nguyên");
export const id = z
  .number({ error: "Vui lòng chọn đối tượng" })
  .int()
  .positive("Vui lòng chọn đối tượng")
  .max(Number.MAX_SAFE_INTEGER);
export const money = z
  .number({ error: "Vui lòng nhập số tiền" })
  .int("Số tiền không có phần thập phân")
  .min(0, "Không được âm")
  .max(999999999999999, "Tối đa 15 chữ số");
