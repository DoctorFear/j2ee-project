import { z } from "zod";

export const dateTime = z.string().refine((value) => {
  const match = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2})(?:\.(\d{1,9}))?)?$/.exec(value);
  if (!match) return false;
  const [, year, month, day, hour, minute, second] = match.map(Number);
  const leap = year % 4 === 0 && (year % 100 !== 0 || year % 400 === 0);
  const days = [31, leap ? 29 : 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
  return (
    month >= 1 &&
    month <= 12 &&
    day >= 1 &&
    day <= days[month - 1] &&
    hour < 24 &&
    minute < 60 &&
    (Number.isNaN(second) || second < 60)
  );
}, "Ngày giờ không hợp lệ");
export function comparableDateTime(value: string) {
  const [date, time = ""] = value.split("T");
  const [clock, fraction = ""] = time.split(".");
  return `${date}T${clock.length === 5 ? `${clock}:00` : clock}.${fraction.padEnd(9, "0")}`;
}
