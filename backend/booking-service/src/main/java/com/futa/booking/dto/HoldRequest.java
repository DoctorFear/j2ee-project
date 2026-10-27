// Thông tin khách hàng yêu cầu giữ chỗ
package com.futa.booking.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record HoldRequest(
    @NotBlank String tripCode,
    @NotBlank String originId,
    @NotBlank String destinationId,
    @NotNull @Size(min = 1, max = 5) List<@NotBlank String> seatCodes,
    @NotBlank @Size(max = 100) String name,
    @NotBlank @Pattern(regexp = "0[0-9]{9}") String phone,
    @NotBlank @Email @Size(max = 150) String email) {}
