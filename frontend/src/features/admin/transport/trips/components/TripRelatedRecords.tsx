import { Link } from "react-router-dom";
import { RelatedRecords } from "../../shared/components/RelatedRecords";
import { tripDriversModule } from "../../trip-drivers/module";
export function TripRelatedRecords({ tripId }: { tripId: number }) {
  return (
    <>
      <RelatedRecords config={tripDriversModule} filter={(entity) => "tripId" in entity && entity.tripId === tripId} />
      <p className="mt-4 text-sm">
        <Link className="text-primary" to={"/admin/trip-statistics/" + tripId}>
          Xem thống kê của chuyến →
        </Link>
      </p>
    </>
  );
}
