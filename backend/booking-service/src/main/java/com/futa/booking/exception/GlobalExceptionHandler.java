// Trả lỗi thống nhất cho các API đặt vé
package com.futa.booking.exception;

import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  // Kiểm tra giới hạn tham số API và danh sách mã chuyến
  @ExceptionHandler({
    jakarta.validation.ConstraintViolationException.class,
    org.springframework.web.method.annotation.HandlerMethodValidationException.class
  })
  ResponseEntity<?> parameters(Exception error) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, "message", "Tham số yêu cầu không hợp lệ."));
  }

  @ExceptionHandler(BusinessException.class)
  ResponseEntity<?> business(BusinessException error) {
    return ResponseEntity.status(error.getStatus())
        .body(Map.of("success", false, "code", error.getCode(), "message", error.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException error) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, "message", "Thông tin đặt vé không hợp lệ"));
  }

  // Từ chối nội dung không đúng định dạng và xung đột dữ liệu
  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
  ResponseEntity<?> malformed(Exception error) {
    return ResponseEntity.badRequest()
        .body(Map.of("success", false, "message", "Nội dung yêu cầu không hợp lệ."));
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(Exception error) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            Map.of(
                "success", false, "message", "Dữ liệu vừa thay đổi. Vui lòng tải lại và thử lại."));
  }
}
