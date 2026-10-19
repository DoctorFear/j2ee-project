package futabus.trip_service.dto.response;

import futabus.trip_service.enums.StationStatus;
import futabus.trip_service.enums.StationType;
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
