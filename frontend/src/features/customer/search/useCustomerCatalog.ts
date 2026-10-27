// Tải danh mục vận tải dùng cho các trang khách hàng
import { useEffect, useState } from "react";
import { bookingApi } from "@/features/customer/api/bookingApi";
import type { CustomerJourney } from "@/features/customer/types/journey";
import type { Station } from "@/features/customer/types/station";
export default function useCustomerCatalog() {
  const [trips, setTrips] = useState<CustomerJourney[]>([]);
  const [stations, setStations] = useState<Station[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  // Bỏ qua kết quả yêu cầu cũ khi người dùng rời trang
  useEffect(() => {
    let active = true;
    Promise.all([bookingApi.trips(), bookingApi.stations()])
      .then(([trips, stations]) => {
        if (active) {
          setTrips(trips);
          setStations(stations);
        }
      })
      .catch((error) => {
        if (active)
          setError(
            error instanceof Error
              ? error.message
              : "Không tải được danh mục chuyến xe.",
          );
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);
  return { trips, stations, loading, error };
}
