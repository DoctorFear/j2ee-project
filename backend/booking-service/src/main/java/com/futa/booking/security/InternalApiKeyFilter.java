// Xác thực khóa gọi dữ liệu Booking giữa các dịch vụ
package com.futa.booking.security;

import com.futa.booking.config.BookingProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {
  private final BookingProperties properties;

  // Chặn API nội bộ nếu thiếu khóa hoặc khóa không trùng cấu hình
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    if (request.getRequestURI().startsWith("/internal/")) {
      String expected = properties.getInternalKey(), supplied = request.getHeader("X-Internal-Key");
      if (expected == null
          || expected.isBlank()
          || supplied == null
          || !MessageDigest.isEqual(
              expected.getBytes(StandardCharsets.UTF_8),
              supplied.getBytes(StandardCharsets.UTF_8))) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response
            .getWriter()
            .write("{\"success\":false,\"message\":\"Không có quyền truy cập API nội bộ.\"}");
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
