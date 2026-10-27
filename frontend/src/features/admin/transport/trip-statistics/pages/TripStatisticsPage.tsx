import { EntityListPage } from "../../shared/pages/EntityListPage";
import { tripStatisticsModule } from "../module";

export default function TripStatisticsPage() {
  return <EntityListPage config={tripStatisticsModule} />;
}
