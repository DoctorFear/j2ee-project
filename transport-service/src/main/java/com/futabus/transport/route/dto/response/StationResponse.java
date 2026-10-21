package com.futabus.transport.route.dto.response;

import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.enums.StationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationResponse {
    Long id;

    String name;

    String address;

    StationType type;

    StationStatus status;
}
