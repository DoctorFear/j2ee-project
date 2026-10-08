// Cấu hình sơ đồ ghế các loại xe
import type { VehicleType } from "../types/vehicle";

// Tạo mã ghế và vị trí theo sức chứa từng loại xe
export const vehicleTypes: VehicleType[] = [34, 22].map((capacity) => ({
  id: `BUS-${capacity}`,
  name: capacity === 34 ? "Giường nằm 34 chỗ" : "Limousine 22 phòng VIP",
  seats: ["A", "B"].flatMap((deck) =>
    Array.from({ length: capacity / 2 }, (_, i) => ({
      code: `${deck}${String(i + 1).padStart(2, "0")}`,
      deck: deck as "A" | "B",
      row: Math.floor(i / 3) + 1,
      position:
        i < 3
          ? ("front" as const)
          : i >= capacity / 2 - 3
            ? ("rear" as const)
            : ("middle" as const),
    })),
  ),
}));
