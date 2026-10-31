// Đọc cấu hình kết nối và thông tin cổng thanh toán
package com.futa.booking.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "booking")
@Getter
@Setter
public class BookingProperties {
  private String transportUrl;
  private String transportZone = "UTC";
  private String frontendUrl;
  private String jwtSecret;
  private String jwtIssuer;
  private String jwtAudience;
  private String emailFrom;
  private String internalKey;
  private boolean salesEnabled = true;
  private java.util.Map<String, java.util.List<String>> vehicleSeats = new java.util.HashMap<>();
  private PayOS payos = new PayOS();

  @Getter
  @Setter
  public static class PayOS {
    private String clientId;
    private String apiKey;
    private String checksumKey;
  }
}
