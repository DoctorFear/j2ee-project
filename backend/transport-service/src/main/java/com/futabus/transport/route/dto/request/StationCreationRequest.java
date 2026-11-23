package com.futabus.transport.route.dto.request;

import com.futabus.transport.route.enums.StationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationCreationRequest {
    @NotBlank
    @Size(max = 150)
    String name;

    @NotBlank
    @Size(max = 255)
    String address;

    @NotNull
    StationType type;
}
