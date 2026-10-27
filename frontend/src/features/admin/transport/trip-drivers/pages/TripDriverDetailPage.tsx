import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { tripDriversModule } from "../module";

export default function TripDriverDetailPage() {
  return <EntityDetailPage config={tripDriversModule}></EntityDetailPage>;
}
