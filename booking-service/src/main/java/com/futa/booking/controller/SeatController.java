// Tra cứu sơ đồ và trạng thái ghế của chuyến xe
package com.futa.booking.controller;

import com.futa.booking.dto.ApiResponse;
import com.futa.booking.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SeatController {
  private final CatalogService catalog;

  @GetMapping("/api/trips/{code}/seats")
  public ApiResponse<?> seats(@PathVariable String code) {
    return ApiResponse.of(catalog.get(code));
  }
}
