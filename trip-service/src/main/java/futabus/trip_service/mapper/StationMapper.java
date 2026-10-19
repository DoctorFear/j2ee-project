package futabus.trip_service.mapper;

import futabus.trip_service.dto.request.StationCreationRequest;
import futabus.trip_service.dto.request.StationUpdateRequest;
import futabus.trip_service.dto.response.StationResponse;
import futabus.trip_service.entity.Station;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface StationMapper {
    Station toStation(StationCreationRequest request);

    StationResponse toStationResponse(Station station);

    void updateStation(StationUpdateRequest request, @MappingTarget Station station);
}
