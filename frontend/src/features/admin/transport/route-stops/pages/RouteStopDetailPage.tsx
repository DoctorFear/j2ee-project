import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { routeStopsModule } from "../module";

export default function RouteStopDetailPage() {
  return <EntityDetailPage config={routeStopsModule}></EntityDetailPage>;
}
