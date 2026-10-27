import { RelatedRecords } from "../../shared/components/RelatedRecords";
import { routeStopsModule } from "../../route-stops/module";
export function RouteStopsPanel({ routeId }: { routeId: number }) {
  return (
    <RelatedRecords config={routeStopsModule} routeId={routeId} manageTo={"/admin/route-stops?routeId=" + routeId} />
  );
}
