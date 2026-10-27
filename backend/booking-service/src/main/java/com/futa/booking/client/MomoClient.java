// Ký yêu cầu và xác minh kết quả thanh toán MoMo
package com.futa.booking.client;

import com.futa.booking.config.BookingProperties;
import com.futa.booking.entity.Payment;
import com.futa.booking.exception.BusinessException;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;

@Component
@RequiredArgsConstructor
public class MomoClient {
  private final BookingProperties properties;
  private final ObjectMapper mapper;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  // Tạo phiên thanh toán với số tiền do máy chủ tính
  public JsonNode create(Payment payment) {
    var config = properties.getMomo();
    if (config.getPartnerCode().isBlank()
        || config.getSecretKey().isBlank()
        || config.getAccessKey().isBlank()
        || config.getIpnUrl().isBlank())
      throw new BusinessException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "PAYMENT_NOT_CONFIGURED",
          "Chưa cấu hình tài khoản MoMo và địa chỉ nhận kết quả thanh toán.");
    String amount = Long.toString(payment.getAmount().longValueExact()),
        orderInfo = "Thanh toán vé FUTA " + payment.getBookingId();
    String redirect =
        properties.getFrontendUrl() + "/trips/payment/book?bookingId=" + payment.getBookingId();
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("partnerCode", config.getPartnerCode());
    body.put("requestId", payment.getRequestId());
    body.put("amount", Long.parseLong(amount));
    body.put("orderId", payment.getId());
    body.put("orderInfo", orderInfo);
    body.put("redirectUrl", redirect);
    body.put("ipnUrl", config.getIpnUrl());
    body.put("requestType", "captureWallet");
    body.put("extraData", "");
    body.put("lang", "vi");
    body.put("autoCapture", true);
    body.put(
        "signature",
        sign(
            "accessKey="
                + config.getAccessKey()
                + "&amount="
                + amount
                + "&extraData=&ipnUrl="
                + config.getIpnUrl()
                + "&orderId="
                + payment.getId()
                + "&orderInfo="
                + orderInfo
                + "&partnerCode="
                + config.getPartnerCode()
                + "&redirectUrl="
                + redirect
                + "&requestId="
                + payment.getRequestId()
                + "&requestType=captureWallet"));
    try {
      var request =
          HttpRequest.newBuilder(URI.create(config.getEndpoint() + "/v2/gateway/api/create"))
              .timeout(Duration.ofSeconds(15))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
              .build();
      var response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw new IllegalStateException();
      var data = mapper.readTree(response.body());
      if (data.path("resultCode").asInt(-1) != 0 || data.path("payUrl").asText("").isBlank())
        throw new BusinessException(
            HttpStatus.BAD_GATEWAY,
            "MOMO_REJECTED",
            "MoMo chưa tạo được phiên thanh toán. Vui lòng thử lại.");
      if (!data.path("orderId").asText().equals(payment.getId())
          || !data.path("requestId").asText().equals(payment.getRequestId())
          || !data.path("partnerCode").asText().equals(config.getPartnerCode())
          || data.path("amount").asLong(-1) != payment.getAmount().longValueExact())
        throw invalid();
      return data;
    } catch (BusinessException e) {
      throw e;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw unavailable();
    } catch (Exception e) {
      throw unavailable();
    }
  }

  // Xác minh toàn bộ trường được MoMo đưa vào chữ ký IPN
  public void verify(JsonNode body) {
    var config = properties.getMomo();
    if (config.getSecretKey().isBlank()
        || !body.path("partnerCode").asText().equals(config.getPartnerCode())) throw invalid();
    String[] keys = {
      "amount",
      "extraData",
      "message",
      "orderId",
      "orderInfo",
      "orderType",
      "partnerCode",
      "payType",
      "requestId",
      "responseTime",
      "resultCode",
      "transId"
    };
    StringBuilder raw = new StringBuilder("accessKey=" + config.getAccessKey());
    for (String key : keys) {
      if (!body.has(key) || body.get(key).isNull()) throw invalid();
      raw.append('&').append(key).append('=').append(body.get(key).asText());
    }
    if (!MessageDigest.isEqual(
        sign(raw.toString()).getBytes(StandardCharsets.UTF_8),
        body.path("signature").asText().getBytes(StandardCharsets.UTF_8))) throw invalid();
  }

  // Hoàn vé hoặc tra cứu yêu cầu cũ với cùng mã chống trùng
  public JsonNode query(Payment payment) {
    var config = properties.getMomo();
    var body = new LinkedHashMap<String, Object>();
    body.put("partnerCode", config.getPartnerCode());
    body.put("requestId", payment.getRequestId());
    body.put("orderId", payment.getId());
    body.put("lang", "vi");
    body.put(
        "signature",
        sign(
            "accessKey="
                + config.getAccessKey()
                + "&orderId="
                + payment.getId()
                + "&partnerCode="
                + config.getPartnerCode()
                + "&requestId="
                + payment.getRequestId()));
    try {
      var request =
          HttpRequest.newBuilder(URI.create(config.getEndpoint() + "/v2/gateway/api/query"))
              .timeout(Duration.ofSeconds(15))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
              .build();
      var response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw unavailable();
      var result = mapper.readTree(response.body());
      if (!result.path("partnerCode").asText().equals(config.getPartnerCode())
          || !result.path("orderId").asText().equals(payment.getId())
          || !result.path("requestId").asText().equals(payment.getRequestId())) throw invalid();
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw unavailable();
    } catch (Exception e) {
      throw unavailable();
    }
  }

  // Gửi hoặc kiểm tra một yêu cầu hoàn tiền theo mã vé
  public JsonNode refund(
      String id, String originalTxn, java.math.BigDecimal amount, boolean query) {
    var config = properties.getMomo();
    String description = "Hoàn tiền vé FUTA";
    var body = new LinkedHashMap<String, Object>();
    body.put("partnerCode", config.getPartnerCode());
    body.put("orderId", id);
    body.put("requestId", id);
    body.put("lang", "vi");
    String raw;
    if (query)
      raw =
          "accessKey="
              + config.getAccessKey()
              + "&orderId="
              + id
              + "&partnerCode="
              + config.getPartnerCode()
              + "&requestId="
              + id;
    else {
      body.put("amount", amount.longValueExact());
      body.put("transId", Long.parseLong(originalTxn));
      body.put("description", description);
      raw =
          "accessKey="
              + config.getAccessKey()
              + "&amount="
              + amount.toPlainString()
              + "&description="
              + description
              + "&orderId="
              + id
              + "&partnerCode="
              + config.getPartnerCode()
              + "&requestId="
              + id
              + "&transId="
              + originalTxn;
    }
    body.put("signature", sign(raw));
    try {
      var request =
          HttpRequest.newBuilder(
                  URI.create(
                      config.getEndpoint() + "/v2/gateway/api/refund" + (query ? "/query" : "")))
              .timeout(Duration.ofSeconds(35))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
              .build();
      var response = http.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) throw unavailable();
      var result = mapper.readTree(response.body());
      if (!result.path("partnerCode").asText().equals(config.getPartnerCode())
          || !result.path("orderId").asText().equals(id)
          || !result.path("requestId").asText().equals(id)) throw invalid();
      return result;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw unavailable();
    } catch (Exception e) {
      throw unavailable();
    }
  }

  private String sign(String value) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(
          new SecretKeySpec(
              properties.getMomo().getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private BusinessException invalid() {
    return new BusinessException(
        HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_SIGNATURE", "Kết quả thanh toán không hợp lệ.");
  }

  private BusinessException unavailable() {
    return new BusinessException(
        HttpStatus.BAD_GATEWAY,
        "MOMO_UNAVAILABLE",
        "Không kết nối được MoMo. Vui lòng kiểm tra lại trạng thái đơn trước khi thử lại.");
  }
}
