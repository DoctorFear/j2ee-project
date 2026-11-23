-- Existing timestamps are UTC wall-clock values. Convert them before the application starts.
-- Two revisions spaced seven hours apart could collide while rows are being updated.
ALTER TABLE station_addresses DROP CONSTRAINT uq_station_address_effective_from;

UPDATE station_addresses
SET effective_from_at = effective_from_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh';

ALTER TABLE station_addresses ADD CONSTRAINT uq_station_address_effective_from
    UNIQUE (station_id, effective_from_at);

UPDATE routes
SET effective_from_at = effective_from_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    effective_to_at = effective_to_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh';

UPDATE trips
SET departure_at = departure_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    arrival_at = arrival_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    published_at = published_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    estimated_departure_at = estimated_departure_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    estimated_arrival_at = estimated_arrival_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    actual_departure_at = actual_departure_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    actual_arrival_at = actual_arrival_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    cancelled_at = cancelled_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh';

UPDATE trip_statistics
SET source_as_of_at = source_as_of_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh',
    updated_at = updated_at AT TIME ZONE 'UTC' AT TIME ZONE 'Asia/Ho_Chi_Minh';
