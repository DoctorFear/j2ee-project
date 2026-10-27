import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { routesModule } from "../module";
import { RouteStopsPanel } from "../components/RouteStopsPanel";

export default function RouteDetailPage() {
  return <EntityDetailPage config={routesModule}>{(id) => <RouteStopsPanel routeId={id} />}</EntityDetailPage>;
}
