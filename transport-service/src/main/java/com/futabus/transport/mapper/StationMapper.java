package com.futabus.transport.mapper;

import com.futabus.transport.dto.request.StationCreationRequest;
import com.futabus.transport.dto.request.StationUpdateRequest;
import com.futabus.transport.dto.response.StationResponse;
import com.futabus.transport.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStation(StationCreationRequest request);

    StationResponse toStationResponse(Station station);

    void updateStation(StationUpdateRequest request, @MappingTarget Station station);
}
