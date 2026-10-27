// Cấu trúc phản hồi dữ liệu thống nhất với frontend
package com.futa.booking.dto;

public record ApiResponse<T>(boolean success, T data) {
  public static <T> ApiResponse<T> of(T value) {
    return new ApiResponse<>(true, value);
  }
}
