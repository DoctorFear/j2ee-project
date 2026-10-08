// Các cơ chế định giá vé
export type Pricing =
  | { mode: "STANDARD" }
  | { mode: "DISCOUNT"; percent: number }
  | { mode: "FIXED"; fullRoutePrice: number };
