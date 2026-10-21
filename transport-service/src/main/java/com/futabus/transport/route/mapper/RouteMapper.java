package com.futabus.transport.route.mapper;

import com.futabus.transport.route.dto.request.RouteCreationRequest;
import com.futabus.transport.route.dto.response.RouteResponse;
import com.futabus.transport.route.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RouteMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Route toRoute(RouteCreationRequest request);

    RouteResponse toRouteResponse(Route route);
}
