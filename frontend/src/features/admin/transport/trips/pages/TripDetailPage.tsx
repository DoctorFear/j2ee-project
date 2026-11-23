import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { tripsModule } from "../module";
import { TripRelatedRecords } from "../components/TripRelatedRecords";
import { TripActions } from "../components/TripActions";
import type { Trip } from "../types";

export default function TripDetailPage() {
  return (
    <EntityDetailPage
      config={tripsModule}
      extraActions={(entity) => <TripActions trip={entity as Trip} />}
    >
      {(id) => <TripRelatedRecords tripId={id} />}
    </EntityDetailPage>
  );
}
