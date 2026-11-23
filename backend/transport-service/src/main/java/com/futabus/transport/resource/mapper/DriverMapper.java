package com.futabus.transport.resource.mapper;

import com.futabus.transport.resource.dto.request.DriverCreationRequest;
import com.futabus.transport.resource.dto.request.DriverUpdateRequest;
import com.futabus.transport.resource.dto.response.DriverResponse;
import com.futabus.transport.resource.entity.Driver;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    Driver toDriver(DriverCreationRequest request);

    DriverResponse toDriverResponse(Driver driver);

    @Mapping(target = "id", ignore = true)
    void updateDriver(DriverUpdateRequest request, @MappingTarget Driver driver);
}
