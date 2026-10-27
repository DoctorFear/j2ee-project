import { EntityDetailPage } from "../../shared/pages/EntityDetailPage";
import { tripsModule } from "../module";
import { TripRelatedRecords } from "../components/TripRelatedRecords";

export default function TripDetailPage() {
  return <EntityDetailPage config={tripsModule}>{(id) => <TripRelatedRecords tripId={id} />}</EntityDetailPage>;
}
