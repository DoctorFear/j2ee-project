package com.futabus.transport.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static com.futabus.transport.common.config.JpaAuditingConfig.VIETNAM_ZONE;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    @Builder.Default
    boolean success = true;

    String message;

    T data;

    @Builder.Default
    LocalDateTime timestamp = LocalDateTime.now(VIETNAM_ZONE).truncatedTo(ChronoUnit.MICROS);
}
