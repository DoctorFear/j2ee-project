package com.futabus.transport.statistics.mapper;

import com.futabus.transport.statistics.dto.request.TripStatisticCreationRequest;
import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import com.futabus.transport.statistics.entity.TripStatistic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripStatisticMapper {
    @Mapping(target = "tripId", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripStatistic toTripStatistic(TripStatisticCreationRequest request);

    TripStatisticResponse toTripStatisticResponse(TripStatistic tripStatistic);
}
