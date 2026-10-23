package com.futabus.transport.route.dto.request;

import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.enums.StationType;
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
