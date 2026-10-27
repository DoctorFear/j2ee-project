import { EntityListPage } from "../../shared/pages/EntityListPage";
import { driversModule } from "../module";

export default function DriversPage() {
  return <EntityListPage config={driversModule} />;
}
