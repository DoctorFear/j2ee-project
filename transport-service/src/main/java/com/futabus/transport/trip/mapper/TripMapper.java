package com.futabus.transport.trip.mapper;

import com.futabus.transport.trip.dto.request.TripCreationRequest;
import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.entity.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    @Mapping(target = "estimatedDepartureAt", ignore = true)
    @Mapping(target = "estimatedArrivalAt", ignore = true)
    @Mapping(target = "actualDepartureAt", ignore = true)
    @Mapping(target = "actualArrivalAt", ignore = true)
    @Mapping(target = "capacitySnapshot", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "saleStatus", ignore = true)
    Trip toTrip(TripCreationRequest request);

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    TripResponse toTripResponse(Trip trip);
}
