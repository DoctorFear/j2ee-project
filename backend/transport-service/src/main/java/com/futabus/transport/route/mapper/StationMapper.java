package com.futabus.transport.route.mapper;

import com.futabus.transport.route.dto.request.StationCreationRequest;
import com.futabus.transport.route.dto.request.StationUpdateRequest;
import com.futabus.transport.route.dto.response.StationResponse;
import com.futabus.transport.route.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStation(StationCreationRequest request);

    StationResponse toStationResponse(Station station);

    void updateStation(StationUpdateRequest request, @MappingTarget Station station);
}
