// API client cho các chức năng phân hệ Tài xế
import { apiClient } from "@/api/client";
import type {
  DriverProfile,
  DriverTrip,
  DriverPassenger,
  DriverTripSummary,
  CheckInRequest,
  CheckInResult,
} from "../types/driver";

export const driverApi = {
  // Lấy hồ sơ tài xế đồng bộ từ danh sách tài xế của Admin
  getProfile: (driverId?: number, phone?: string) => {
    const params = new URLSearchParams();
    if (driverId) params.append("driverId", driverId.toString());
    if (phone) params.append("phone", phone);
    const queryString = params.toString() ? `?${params.toString()}` : "";
    return apiClient<DriverProfile>(`/driver/trips/profile${queryString}`);
  },

  // Lấy lịch ca trực theo ngày
  getTrips: (date?: string, driverId?: number, phone?: string) => {
    const params = new URLSearchParams();
    if (date) params.append("date", date);
    if (driverId) params.append("driverId", driverId.toString());
    if (phone) params.append("phone", phone);
    const queryString = params.toString() ? `?${params.toString()}` : "";
    return apiClient<DriverTrip[]>(`/driver/trips${queryString}`);
  },

  // Cập nhật trạng thái chuyến xe (RUNNING hoặc COMPLETED)
  updateTripStatus: (tripCode: string, status: "RUNNING" | "COMPLETED") => {
    return apiClient<DriverTrip>(`/driver/trips/${encodeURIComponent(tripCode)}/status?status=${status}`, {
      method: "PUT",
    });
  },

  // Lấy danh sách hành khách theo chuyến
  getPassengers: (tripCode: string) => {
    return apiClient<DriverPassenger[]>(`/driver/trips/${encodeURIComponent(tripCode)}/passengers`);
  },

  // Lấy tóm tắt tiến độ lên xe
  getSummary: (tripCode: string) => {
    return apiClient<DriverTripSummary>(`/driver/trips/${encodeURIComponent(tripCode)}/summary`);
  },

  // Tra cứu vé
  lookupTicket: (tripCode: string, ticketCode: string) => {
    return apiClient<DriverPassenger>(`/driver/trips/${encodeURIComponent(tripCode)}/tickets/${encodeURIComponent(ticketCode)}`);
  },

  // Soát vé (QR, mã vé hoặc 4 số cuối SĐT)
  checkIn: (tripCode: string, request: CheckInRequest) => {
    return apiClient<CheckInResult>(`/driver/trips/${encodeURIComponent(tripCode)}/check-in`, {
      method: "POST",
      data: request,
    });
  },

  // Đổi trạng thái 1 chạm giữa Chờ lên xe và Đã lên xe
  toggleBoarded: (tripCode: string, ticketCode: string) => {
    return apiClient<DriverPassenger>(`/driver/trips/${encodeURIComponent(tripCode)}/passengers/${encodeURIComponent(ticketCode)}/toggle`, {
      method: "POST",
    });
  },
};
