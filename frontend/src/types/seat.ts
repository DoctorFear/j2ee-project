// Cấu hình ghế và vị trí trên xe
export type Seat = {
  code: string;
  deck: "A" | "B";
  row: number;
  position: "front" | "middle" | "rear";
};
