import type { Driver } from "../../drivers/types";
import type { TransportRoute } from "../../routes/types";
import type { RouteStop } from "../../route-stops/types";
import type { Station } from "../../stations/types";
import type { Trip } from "../../trips/types";
import type { TripDriver } from "../../trip-drivers/types";
import type { TripStatistic } from "../../trip-statistics/types";
import type { Vehicle } from "../../vehicles/types";

export interface EntityMap {
  stations: Station;
  routes: TransportRoute;
  "route-stops": RouteStop;
  vehicles: Vehicle;
  drivers: Driver;
  trips: Trip;
  "trip-drivers": TripDriver;
  "trip-statistics": TripStatistic;
}

export type Resource = keyof EntityMap;
export type TransportEntity = EntityMap[Resource];
