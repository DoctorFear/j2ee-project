import { EntityListPage } from "../../shared/pages/EntityListPage";
import { stationsModule } from "../module";

export default function StationsPage() {
  return <EntityListPage config={stationsModule} />;
}
