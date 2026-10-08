// Tiêu chí tìm kiếm chuyến xe của khách hàng
export type SearchCriteria = {
  originId: string;
  destinationId: string;
  date: string;
  timeRanges: number[];
  vehicleTypes: string[];
  position: string;
  deck: string;
  minPrice: string;
  maxPrice: string;
  sort: "time" | "price" | "seats";
};
