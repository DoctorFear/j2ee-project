package com.futabus.transport.resource.dto.response;

import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VehicleResponse {
    Long id;

    String licensePlate;

    ServiceType type;

    int capacity;

    VehicleStatus status;
}
