import { EntityListPage } from "../../shared/pages/EntityListPage";
import { tripDriversModule } from "../module";

export default function TripDriversPage() {
  return <EntityListPage config={tripDriversModule} />;
}
