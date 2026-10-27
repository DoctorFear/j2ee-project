import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { driversModule } from "../module";

export default function DriverDetailPage() {
  return <EntityDetailPage config={driversModule}></EntityDetailPage>;
}
