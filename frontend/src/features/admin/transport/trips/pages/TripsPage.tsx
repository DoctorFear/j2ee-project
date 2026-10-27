import { EntityListPage } from "../../shared/pages/EntityListPage";
import { tripsModule } from "../module";

export default function TripsPage() {
  return <EntityListPage config={tripsModule} />;
}
