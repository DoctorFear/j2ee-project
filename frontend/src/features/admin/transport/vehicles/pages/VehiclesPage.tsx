import { EntityListPage } from "../../shared/pages/EntityListPage";
import { vehiclesModule } from "../module";

export default function VehiclesPage() {
  return <EntityListPage config={vehiclesModule} />;
}
