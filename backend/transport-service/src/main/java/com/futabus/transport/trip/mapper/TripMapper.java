package com.futabus.transport.trip.mapper;

import com.futabus.transport.trip.dto.response.TripResponse;
import com.futabus.transport.trip.entity.Trip;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "vehicleId", source = "vehicle.id")
    TripResponse toTripResponse(Trip trip);
}
