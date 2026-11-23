package com.futabus.transport.route.mapper;

import com.futabus.transport.route.dto.request.RouteStopCreationRequest;
import com.futabus.transport.route.dto.request.RouteStopUpdateRequest;
import com.futabus.transport.route.dto.response.RouteStopResponse;
import com.futabus.transport.route.entity.RouteStop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RouteStopMapper {
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "stationId", source = "station.id")
    RouteStopResponse toRouteStopResponse(RouteStop routeStop);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "station", ignore = true)
    RouteStop toRouteStop(RouteStopCreationRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "station", ignore = true)
    void updateRouteStop(RouteStopUpdateRequest request, @MappingTarget RouteStop stop);
}
