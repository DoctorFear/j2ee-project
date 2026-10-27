// API vé điện tử chỉ trả dữ liệu của chủ vé
package com.futa.booking.controller;

import com.futa.booking.dto.*;
import com.futa.booking.service.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {
  private final TicketService tickets;
  private final RefundService refunds;

  @PostMapping("/{code}/cancel")
  public ApiResponse<?> cancel(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.of(refunds.request(code, jwt.getSubject()));
  }

  @GetMapping("/{code}/refund")
  public ApiResponse<?> refund(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.of(refunds.status(code, jwt.getSubject()));
  }

  @GetMapping("/my")
  public ApiResponse<?> mine(@AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.of(tickets.views(jwt.getSubject()));
  }

  @GetMapping("/{code}")
  public ApiResponse<?> get(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.of(tickets.detail(code, jwt.getSubject()));
  }

  @GetMapping("/{code}/qr")
  public ApiResponse<?> qr(@PathVariable String code, @AuthenticationPrincipal Jwt jwt) {
    return ApiResponse.of(tickets.qr(code, jwt.getSubject()));
  }
}
