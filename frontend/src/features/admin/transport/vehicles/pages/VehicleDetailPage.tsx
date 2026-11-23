import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { vehiclesModule } from "../module";
import { VehicleStatusAction } from "../components/VehicleStatusAction";
import type { Vehicle } from "../types";

export default function VehicleDetailPage() {
  return (
    <EntityDetailPage
      config={vehiclesModule}
      extraActions={(entity) => <VehicleStatusAction vehicle={entity as Vehicle} />}
    />
  );
}
