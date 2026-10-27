import { EntityListPage } from "../../shared/pages/EntityListPage";
import { routeStopsModule } from "../module";
import { RouteStopRouteFilter } from "../components/RouteStopRouteFilter";

export default function RouteStopsPage() {
  return <EntityListPage config={routeStopsModule} filters={<RouteStopRouteFilter />} />;
}
