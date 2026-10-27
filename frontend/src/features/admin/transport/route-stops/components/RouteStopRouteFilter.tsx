import { entityLabel } from "../../shared/model/entity-label";
import { useRouteStopFilter } from "../hooks/useRouteStopFilter";
export function RouteStopRouteFilter() {
  const { routes, routeId, changeRoute } = useRouteStopFilter();
  return (
    <>
      <select
        className="h-9 max-w-72 rounded-md border px-3 text-sm"
        aria-label="Lọc tuyến"
        value={routeId}
        onChange={(event) => changeRoute(event.target.value)}
      >
        <option value="">Tất cả tuyến</option>
        {routes.data?.map((route) => (
          <option key={route.id} value={route.id}>
            {entityLabel(route)}
          </option>
        ))}
      </select>
      {routes.isError && (
        <p role="alert" className="text-sm text-destructive">
          Không tải được bộ lọc tuyến: {routes.error.message}
        </p>
      )}
    </>
  );
}
