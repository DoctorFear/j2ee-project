package futabus.trip_service.dto.request;

import futabus.trip_service.enums.StationStatus;
import futabus.trip_service.enums.StationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StationUpdateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String address;

    @NotNull
    private StationType type;

    @NotNull
    private StationStatus status;
}
