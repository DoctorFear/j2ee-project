import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { stationsModule } from "../module";

export default function StationDetailPage() {
  return <EntityDetailPage config={stationsModule}></EntityDetailPage>;
}
