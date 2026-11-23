package com.futabus.transport.resource.mapper;

import com.futabus.transport.resource.dto.request.VehicleCreationRequest;
import com.futabus.transport.resource.dto.request.VehicleUpdateRequest;
import com.futabus.transport.resource.dto.response.VehicleResponse;
import com.futabus.transport.resource.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Vehicle toVehicle(VehicleCreationRequest request);

    VehicleResponse toVehicleResponse(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    void updateVehicle(VehicleUpdateRequest request, @MappingTarget Vehicle vehicle);
}
