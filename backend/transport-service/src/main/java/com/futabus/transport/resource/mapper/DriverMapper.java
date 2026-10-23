package com.futabus.transport.resource.mapper;

import com.futabus.transport.resource.dto.request.DriverCreationRequest;
import com.futabus.transport.resource.dto.response.DriverResponse;
import com.futabus.transport.resource.entity.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    Driver toDriver(DriverCreationRequest request);

    DriverResponse toDriverResponse(Driver driver);
}
