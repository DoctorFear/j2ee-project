import { lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { PageLoading } from "@/components/shared/PageLoading";
import { CustomerRoute } from "./guards/CustomerRoute";
import { ProtectedRoute } from "./guards/ProtectedRoute";

const CustomerHeader = lazy(() => import("./layouts/customer/CustomerHeader"));
const AdminLayout = lazy(() => import("./layouts/admin/AdminLayout"));
const SearchTripPage = lazy(() => import("@/features/customer/search/SearchTripPage"));
const SeatBookingPage = lazy(() => import("@/features/customer/booking/SeatBookingPage"));
const MyTicketsPage = lazy(() => import("@/features/customer/tickets/MyTicketsPage"));
const PaymentResultPage = lazy(() => import("@/features/customer/booking/PaymentResultPage"));
const LoginPage = lazy(() => import("@/features/auth/pages/LoginPage"));
const RegisterPage = lazy(() => import("@/features/auth/pages/RegisterPage"));
const ForgotPasswordPage = lazy(() => import("@/features/auth/pages/ForgotPasswordPage"));
const ResetPasswordPage = lazy(() => import("@/features/auth/pages/ResetPasswordPage"));
const ProfilePage = lazy(() => import("@/features/account/ProfilePage"));
const DriverPage = lazy(() => import("@/features/driver/DriverPage"));
const DashboardPage = lazy(() => import("@/features/admin/dashboard/DashboardPage"));
const AdminAccountPage = lazy(() => import("@/features/admin/account/AdminAccountPage"));
const StationsPage = lazy(() => import("@/features/admin/transport/stations/pages/StationsPage"));
const StationDetailPage = lazy(() => import("@/features/admin/transport/stations/pages/StationDetailPage"));
const RoutesPage = lazy(() => import("@/features/admin/transport/routes/pages/RoutesPage"));
const RouteDetailPage = lazy(() => import("@/features/admin/transport/routes/pages/RouteDetailPage"));
const RouteStopsPage = lazy(() => import("@/features/admin/transport/route-stops/pages/RouteStopsPage"));
const RouteStopDetailPage = lazy(() => import("@/features/admin/transport/route-stops/pages/RouteStopDetailPage"));
const VehiclesPage = lazy(() => import("@/features/admin/transport/vehicles/pages/VehiclesPage"));
const VehicleDetailPage = lazy(() => import("@/features/admin/transport/vehicles/pages/VehicleDetailPage"));
const DriversPage = lazy(() => import("@/features/admin/transport/drivers/pages/DriversPage"));
const DriverDetailPage = lazy(() => import("@/features/admin/transport/drivers/pages/DriverDetailPage"));
const TripsPage = lazy(() => import("@/features/admin/transport/trips/pages/TripsPage"));
const TripDetailPage = lazy(() => import("@/features/admin/transport/trips/pages/TripDetailPage"));
const TripDriversPage = lazy(() => import("@/features/admin/transport/trip-drivers/pages/TripDriversPage"));
const TripDriverDetailPage = lazy(() => import("@/features/admin/transport/trip-drivers/pages/TripDriverDetailPage"));
const TripStatisticsPage = lazy(() => import("@/features/admin/transport/trip-statistics/pages/TripStatisticsPage"));
const TripStatisticDetailPage = lazy(
  () => import("@/features/admin/transport/trip-statistics/pages/TripStatisticDetailPage")
);
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));

export function AppRoutes() {
  return (
    <Suspense fallback={<PageLoading />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        <Route
          path="/"
          element={
            <CustomerRoute>
              <SearchTripPage header={<CustomerHeader />} />
            </CustomerRoute>
          }
        />

        <Route
          path="/trips/:code/book"
          element={
            <CustomerRoute>
              <SeatBookingPage header={<CustomerHeader booking />} />
            </CustomerRoute>
          }
        />
        <Route
          path="/trips/payment/book"
          element={
            <ProtectedRoute requiredRole="Member">
              <PaymentResultPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/my-tickets"
          element={
            <ProtectedRoute requiredRole="Member">
              <MyTicketsPage header={<CustomerHeader />} />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute requiredRole="Member">
              <ProfilePage header={<CustomerHeader />} />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRole="Admin">
              <AdminLayout />
            </ProtectedRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="account" element={<AdminAccountPage />} />
          <Route path="stations" element={<StationsPage />} />
          <Route path="stations/:id" element={<StationDetailPage />} />
          <Route path="routes" element={<RoutesPage />} />
          <Route path="routes/:id" element={<RouteDetailPage />} />
          <Route path="route-stops" element={<RouteStopsPage />} />
          <Route path="route-stops/:id" element={<RouteStopDetailPage />} />
          <Route path="vehicles" element={<VehiclesPage />} />
          <Route path="vehicles/:id" element={<VehicleDetailPage />} />
          <Route path="drivers" element={<DriversPage />} />
          <Route path="drivers/:id" element={<DriverDetailPage />} />
          <Route path="trips" element={<TripsPage />} />
          <Route path="trips/:id" element={<TripDetailPage />} />
          <Route path="trip-drivers" element={<TripDriversPage />} />
          <Route path="trip-drivers/:id" element={<TripDriverDetailPage />} />
          <Route path="trip-statistics" element={<TripStatisticsPage />} />
          <Route path="trip-statistics/:id" element={<TripStatisticDetailPage />} />
          <Route path="reports" element={<Navigate to="/admin/trip-statistics" replace />} />
          <Route path="*" element={<Navigate to="/admin" replace />} />
        </Route>

        <Route
          path="/driver"
          element={
            <ProtectedRoute requiredRole="Driver">
              <DriverPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/driver/trips"
          element={
            <ProtectedRoute requiredRole="Driver">
              <DriverPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  );
}
