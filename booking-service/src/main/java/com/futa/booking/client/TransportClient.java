// Đọc dữ liệu vận tải qua các API hiện có
package com.futa.booking.client;

import com.futa.booking.config.BookingProperties;
import com.futa.booking.exception.BusinessException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;

@Component
@RequiredArgsConstructor
public class TransportClient {
  private final BookingProperties properties;
  private final ObjectMapper mapper;
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  // Đọc danh mục vận tải và kiểm tra định dạng phản hồi
  public List<JsonNode> list(String resource) {
    try {
      var request =
          HttpRequest.newBuilder(URI.create(properties.getTransportUrl() + "/api/" + resource))
              .timeout(Duration.ofSeconds(10))
              .GET()
              .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw new IllegalStateException();
      JsonNode data = mapper.readTree(response.body()).get("data");
      if (data == null || !data.isArray()) throw new IllegalStateException();
      List<JsonNode> result = new ArrayList<>();
      data.forEach(result::add);
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw unavailable();
    } catch (Exception e) {
      throw unavailable();
    }
  }

  private BusinessException unavailable() {
    return new BusinessException(
        HttpStatus.SERVICE_UNAVAILABLE,
        "TRANSPORT_UNAVAILABLE",
        "Không đọc được dữ liệu chuyến xe. Vui lòng thử lại.");
  }
}
