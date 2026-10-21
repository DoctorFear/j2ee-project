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

The existing Postman collection in `../postman/` includes requests for both
resources, directly and through Gateway. Create a station and route before
creating a stop; delete stops before deleting their parent records.

Run `./mvnw test` (Windows: `.\mvnw.cmd test`) to verify the API against H2.
