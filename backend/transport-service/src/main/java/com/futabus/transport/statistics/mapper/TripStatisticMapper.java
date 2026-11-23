package com.futabus.transport.statistics.mapper;

import com.futabus.transport.statistics.dto.request.TripStatisticIngestRequest;
import com.futabus.transport.statistics.dto.response.TripStatisticResponse;
import com.futabus.transport.statistics.entity.TripStatistic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TripStatisticMapper {
    @Mapping(target = "tripId", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TripStatistic toTripStatistic(TripStatisticIngestRequest request);

    TripStatisticResponse toTripStatisticResponse(TripStatistic tripStatistic);

    @Mapping(target = "tripId", ignore = true)
    @Mapping(target = "trip", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateStatistic(TripStatisticIngestRequest request, @MappingTarget TripStatistic statistic);
}
