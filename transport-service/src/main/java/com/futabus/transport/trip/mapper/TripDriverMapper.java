package com.futabus.transport.trip.mapper;

import com.futabus.transport.trip.dto.request.TripDriverCreationRequest;
import com.futabus.transport.trip.dto.response.TripDriverResponse;
import com.futabus.transport.trip.entity.TripDriver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripDriverMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "driver", ignore = true)
    TripDriver toTripDriver(TripDriverCreationRequest request);

    @Mapping(target = "tripId", source = "trip.id")
    @Mapping(target = "driverId", source = "driver.id")
    TripDriverResponse toTripDriverResponse(TripDriver tripDriver);
}
