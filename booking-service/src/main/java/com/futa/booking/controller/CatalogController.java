// Cung cấp danh mục và lịch trình cho khách hàng
package com.futa.booking.controller;

import com.futa.booking.dto.ApiResponse;
import com.futa.booking.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings/catalog")
@RequiredArgsConstructor
public class CatalogController {
  private final CatalogService catalog;

  @GetMapping("/stations")
  public ApiResponse<?> stations() {
    return ApiResponse.of(catalog.stations());
  }

  @GetMapping("/trips")
  public ApiResponse<?> trips() {
    return ApiResponse.of(catalog.all());
  }

  @GetMapping("/trips/{code}")
  public ApiResponse<?> trip(@PathVariable String code) {
    return ApiResponse.of(catalog.get(code));
  }
}
