import { EntityListPage } from "../../shared/pages/EntityListPage";
import { routesModule } from "../module";

export default function RoutesPage() {
  return <EntityListPage config={routesModule} />;
}
