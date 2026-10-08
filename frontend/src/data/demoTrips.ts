// Dữ liệu chuyến xe cho các chức năng khách hàng
import type { Trip } from "../types/trip";
import { stations } from "./stations";
import { localDate } from "../utils/localDate";
// Tạo chuỗi trạm tích lũy cho hai chiều tuyến
const outbound = stations.map((s, i) => ({
  stationId: s.id,
  order: i,
  distanceKm: [0, 35, 120, 620, 650][i],
  offsetMinutes: [0, 40, 150, 690, 740][i],
  active: true,
}));
const inbound = [...stations].reverse().map((s, i) => ({
  stationId: s.id,
  order: i,
  distanceKm: [0, 30, 530, 615, 650][i],
  offsetMinutes: [0, 50, 590, 700, 740][i],
  active: true,
}));
// Sinh lịch chuyến hai chiều theo ngày
export const trips: Trip[] = [-2, 0, 1, 2, 3, 4, 5, 6].flatMap((day) =>
  ["09:15", "13:45", "17:30", "20:00"].flatMap((time, i) =>
    [false, true].map((reverse) => ({
      rating: [4.5, 4.7, 4.6, 4.8][i],
      code: `FT-${localDate(day).replaceAll("-", "")}-${reverse ? "R" : "D"}${i + 1}`,
      routeName: reverse ? "Miền Tây → An Nhơn" : "An Nhơn → Miền Tây",
      departureAt: `${localDate(day)}T${time}:00+07:00`,
      stops: reverse ? inbound : outbound,
      vehicleTypeId: i % 2 ? "BUS-22" : "BUS-34",
      pricePerKm: 600,
      pricing:
        i === 1
          ? { mode: "DISCOUNT" as const, percent: 15 }
          : i === 3
            ? { mode: "FIXED" as const, fullRoutePrice: 450000 }
            : { mode: "STANDARD" as const },
      saleStatus: "OPEN" as const,
      soldSeats: ["A02", "A05", "B03", "B07"],
      heldSeats: ["A03"],
    })),
  ),
);
