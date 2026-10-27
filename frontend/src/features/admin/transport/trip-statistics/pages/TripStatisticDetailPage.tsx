import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { tripStatisticsModule } from "../module";

export default function TripStatisticDetailPage() {
  return <EntityDetailPage config={tripStatisticsModule}></EntityDetailPage>;
}
