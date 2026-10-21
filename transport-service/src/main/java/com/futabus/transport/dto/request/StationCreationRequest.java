package com.futabus.transport.dto.request;

import com.futabus.transport.enums.StationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationCreationRequest {
    @NotBlank
    @Size(max = 255)
    String name;

    @NotBlank
    @Size(max = 255)
    String address;

    @NotNull
    StationType type;
}
