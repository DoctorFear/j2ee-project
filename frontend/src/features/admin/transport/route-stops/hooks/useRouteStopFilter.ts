import { useSearchParams } from "react-router-dom";
import { useEntityList } from "../../shared/hooks/useEntityList";
import { routesApi } from "../../routes/api";
export function useRouteStopFilter() {
  const [params, setParams] = useSearchParams();
  const routes = useEntityList(routesApi);
  const routeId = params.get("routeId") ?? "";
  const changeRoute = (value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set("routeId", value);
    else next.delete("routeId");
    next.delete("page");
    setParams(next, { replace: true });
  };
  return { routes, routeId, changeRoute };
}
