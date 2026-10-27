# Route and RouteStop API

Base URL: `http://localhost:8081`, or `http://localhost:8080` through Gateway.
These endpoints use the same `ApiResponse` envelope as Station:
`{"success":true,"data":...,"timestamp":"..."}`.
Errors use `{"success":false,"message":"...","timestamp":"..."}`.

| Resource  | Create                  | List                   | Detail                      | Replace                     | Delete                         |
| --------- | ----------------------- | ---------------------- | --------------------------- | --------------------------- | ------------------------------ |
| Route     | `POST /api/routes`      | `GET /api/routes`      | `GET /api/routes/{id}`      | `PUT /api/routes/{id}`      | `DELETE /api/routes/{id}`      |
| RouteStop | `POST /api/route-stops` | `GET /api/route-stops` | `GET /api/route-stops/{id}` | `PUT /api/route-stops/{id}` | `DELETE /api/route-stops/{id}` |

Create returns **201** and a `Location` header; reads and updates return **200**;
delete returns **204** with no body. No pagination is applied, matching Station.
No additional authentication is introduced by these controllers.

## Route

Create body:

```json
{
  "code": "HCM-DL",
  "versionNo": 1,
  "name": "Ho Chi Minh - Da Lat",
  "basePrice": 250000,
  "effectiveFromAt": "2026-10-21T08:00:00",
  "effectiveToAt": null
}
```

All fields except `effectiveToAt` are required. `code` and `name` must be nonblank,
at most 255 characters. `versionNo` must be positive. `basePrice` must be
nonnegative, at most 15 integer digits, with no fractional amount.
Dates use local date/time without a timezone. If supplied, `effectiveToAt` must
be strictly after `effectiveFromAt`.

A route starts with `status: "INACTIVE"`. PUT requires the same fields plus
`status: "ACTIVE"` or `"INACTIVE"`. Omit or set `effectiveToAt` to null to remove
its previous value. Responses contain these fields plus `id` and `status`.

The `(code, versionNo)` pair is unique. Duplicate creation or updates return
**409**. A route with existing stops cannot be deleted (**409**); delete its
stops first. Missing resource IDs return **404**. Invalid bodies return **400**.

## RouteStop

Both POST and PUT require every field below (including both booleans):

```json
{
  "routeId": 1,
  "stationId": 1,
  "stopOrder": 0,
  "distanceKm": 0,
  "offsetMinutes": 0,
  "allowPickup": true,
  "allowDropOff": false
}
```

`routeId` and `stationId` must be positive IDs of existing records; missing
references return **404**. `stopOrder` and `offsetMinutes` are nonnegative
integers. `distanceKm` is nonnegative, with at most 8 integer and 2 fractional
digits. Distance and offset describe cumulative values from the route origin;
this CRUD does not enforce monotonicity between different stops or renumber them.

Responses contain the fields above plus `id`; they do not serialize JPA entities.
PUT can change the route or station reference. `(routeId, stopOrder)` must be
unique; conflicts return **409**, including when moving a stop to another route.
The same station can appear in multiple stops. Invalid bodies return **400**.

`GET /api/route-stops?routeId=1` lists one route's stops by ascending `stopOrder`.
A nonexistent route returns **404**; an existing route with no stops returns an
empty array. Without the filter, results are ordered by `routeId`, then
`stopOrder`. Missing stop IDs return **404**. Deleting a stop preserves its route
and station. A station referenced by a stop cannot be deleted (**409**).

The existing Postman collection in `../postman` includes requests for both
resources, directly and through Gateway. Create a station and route before
creating a stop; delete stops before deleting their parent records.

Run `./mvnw test` (Windows: `.\mvnw.cmd test`) to verify the API against H2.

## Vehicle and Driver

These resources follow Station's response envelope and status codes, with only
create, list, detail, and delete operations. PUT and PATCH are not supported.
Both resources are also routed through Gateway.

| Resource | Create | List | Detail | Delete |
| -------- | ------ | ---- | ------ | ------ |
| Vehicle | `POST /api/vehicles` | `GET /api/vehicles` | `GET /api/vehicles/{id}` | `DELETE /api/vehicles/{id}` |
| Driver | `POST /api/drivers` | `GET /api/drivers` | `GET /api/drivers/{id}` | `DELETE /api/drivers/{id}` |

Vehicle create body (all fields required):

```json
{
  "licensePlate": "51B-12345",
  "type": "SEAT",
  "capacity": 45
}
```

`licensePlate` must be nonblank, at most 255 characters. `type` accepts `SEAT`,
`BERTH`, or `LIMOUSINE`; `capacity` must be a positive integer.

Driver create body:

```json
{
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "licenseNumber": "GPLX123",
  "licenseClass": "D",
  "licenseExpiryOn": "2030-01-01T00:00:00"
}
```

`fullName` and `phone` are required and nonblank. All string fields have a
255-character limit. License fields are optional; `licenseExpiryOn` uses local
date/time without a timezone.

New vehicles and drivers start with `status: "INACTIVE"`. Responses contain the
creation fields plus `id` and `status`. POST returns **201** with a `Location`
header, GET returns **200**, and DELETE returns **204** with no body. Lists have
no pagination. Invalid input returns **400**; reading or deleting a missing ID
returns **404**.

## Trip and TripDriver

These resources follow Station's response envelope: POST returns **201** with a
`Location` header, GET returns **200**, and DELETE returns **204** with no body.
Only create, list, detail, and delete are supported; PUT and PATCH return **405**.

| Resource | Create / List | Detail / Delete |
| -------- | ------------- | --------------- |
| Trip | `POST /api/trips`, `GET /api/trips` | `GET /api/trips/{id}`, `DELETE /api/trips/{id}` |
| TripDriver | `POST /api/trip-drivers`, `GET /api/trip-drivers` | `GET /api/trip-drivers/{id}`, `DELETE /api/trip-drivers/{id}` |

Both resources are routed through Gateway. Lists have no pagination.

Trip create body (all fields required):

```json
{
  "code": "HCM-DL-001",
  "routeId": 1,
  "vehicleId": 1,
  "departureAt": "2026-10-23T08:00:00",
  "arrivalAt": "2026-10-23T14:00:00",
  "ticketPrice": 250000
}
```

`code` must be nonblank and at most 255 characters. Reference IDs must be positive
and exist. `arrivalAt` must be after `departureAt`; both use local date/time without
a timezone. `ticketPrice` is nonnegative with at most 15 integer digits and no
fraction. New trips use `status: "DRAFT"`, `saleStatus: "NOT_OPEN"`, and a
`capacitySnapshot` copied from the selected vehicle. Responses include these
fields plus `id` and the estimated/actual timestamps, initially null.

TripDriver create body (all fields required):

```json
{
  "tripId": 1,
  "driverId": 1,
  "role": "PRIMARY"
}
```

`tripId` and `driverId` must be positive IDs of existing records. `role` accepts
`PRIMARY` or `RELIEF`. Responses contain these fields plus `id`. Assigning the
same driver to the same trip again returns **409**, regardless of role.

Invalid input returns **400**. Missing resources or references return **404**.
Delete a trip's driver assignments before deleting the trip; otherwise deletion
returns **409**. Deleting an assignment preserves the trip and driver; deleting
a trip preserves its route and vehicle.
