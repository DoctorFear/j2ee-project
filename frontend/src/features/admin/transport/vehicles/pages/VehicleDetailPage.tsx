import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { vehiclesModule } from "../module";

export default function VehicleDetailPage() {
  return <EntityDetailPage config={vehiclesModule}></EntityDetailPage>;
}
