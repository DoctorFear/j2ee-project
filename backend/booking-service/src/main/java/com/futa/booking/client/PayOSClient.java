// Khởi tạo link thanh toán VietQR và xác minh Webhook từ PayOS
package com.futa.booking.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futa.booking.config.BookingProperties;
import com.futa.booking.entity.Payment;
import com.futa.booking.exception.BusinessException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;

@Slf4j
@Component
public class PayOSClient {

  // Kết quả trả về được gói lại để PaymentService không phụ thuộc vào kiểu dữ liệu của SDK
  public record PayLink(String checkoutUrl, String qrCode) {}

  public record PaymentInfo(String status, long amount) {}

  public record PaidEvent(long orderCode, long amount, String reference) {}

  private final BookingProperties properties;
  private final PayOS payOS;
  private final ObjectMapper objectMapper =
          new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  public PayOSClient(BookingProperties properties) {
    this.properties = properties;
    var config = properties.getPayos();
    if (config.getClientId() == null || config.getClientId().isBlank()
            || config.getApiKey() == null || config.getApiKey().isBlank()
            || config.getChecksumKey() == null || config.getChecksumKey().isBlank()) {
      this.payOS = null;
    } else {
      this.payOS = new PayOS(config.getClientId(), config.getApiKey(), config.getChecksumKey());
    }
  }

  // Tạo liên kết thanh toán VietQR PayOS
  public PayLink create(Payment payment, long orderCode) {
    if (payOS == null) {
      throw new BusinessException(
              HttpStatus.SERVICE_UNAVAILABLE,
              "PAYMENT_NOT_CONFIGURED",
              "Chưa cấu hình thông số kết nối PayOS (client-id, api-key, checksum-key).");
    }

    try {
      long amount = payment.getAmount().longValueExact();
      String description = ("FUTA " + payment.getBookingId()).replaceAll("[^a-zA-Z0-9 ]", "");
      if (description.length() > 25) {
        description = description.substring(0, 25);
      }

      String returnUrl = properties.getFrontendUrl() + "/trips/payment/book?bookingId=" + payment.getBookingId();
      String cancelUrl = returnUrl;

      PaymentLinkItem item = PaymentLinkItem.builder()
              .name("Vé xe FUTA")
              .quantity(1)
              .price(amount)
              .build();

      CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
              .orderCode(orderCode)
              .amount(amount)
              .description(description)
              .returnUrl(returnUrl)
              .cancelUrl(cancelUrl)
              .item(item)
              .build();

      var link = payOS.paymentRequests().create(paymentData);
      return new PayLink(link.getCheckoutUrl(), link.getQrCode());
    } catch (Exception e) {
      log.error("PayOS tạo link thanh toán thất bại, orderCode={}", orderCode, e);
      throw new BusinessException(
              HttpStatus.BAD_GATEWAY,
              "PAYOS_REJECTED",
              "PayOS chưa tạo được phiên thanh toán. Vui lòng thử lại: " + e.getMessage());
    }
  }

  // Xác minh chữ ký dữ liệu Webhook từ PayOS
  public PaidEvent verifyWebhook(JsonNode body) {
    if (payOS == null) {
      throw invalid();
    }
    try {
      // Chuyển đổi dữ liệu JsonNode sang dạng Map JSON mà PayOS SDK chấp nhận trước khi kiểm tra chữ ký
      Map<String, Object> webhook =
              objectMapper.readValue(body.toString(), new TypeReference<Map<String, Object>>() {});
      var data = payOS.webhooks().verify(webhook);
      return new PaidEvent(data.getOrderCode(), data.getAmount(), data.getReference());
    } catch (Exception e) {
      log.warn("Webhook PayOS không hợp lệ: {}", e.getMessage());
      throw invalid();
    }
  }

  // Tra cứu thông tin thanh toán theo orderCode
  public PaymentInfo getPaymentLinkInformation(long orderCode) {
    if (payOS == null) throw unavailable();
    try {
      var info = payOS.paymentRequests().get(orderCode);
      return new PaymentInfo(String.valueOf(info.getStatus()), info.getAmount());
    } catch (Exception e) {
      log.error("PayOS tra cứu thông tin thanh toán thất bại, orderCode={}", orderCode, e);
      throw unavailable();
    }
  }

  // Hủy, Hoàn tiền
  public boolean cancelPaymentLink(long orderCode, String reason) {
    if (payOS == null) return true;
    try {
      payOS.paymentRequests().cancel(orderCode, reason);
      return true;
    } catch (Exception e) {
      log.warn("PayOS hủy link thanh toán không thành công, orderCode={}: {}", orderCode, e.getMessage());
      return true; // Chấp nhận hủy thành công đối với môi trường sandbox/hoàn tiền thủ công
    }
  }

  private BusinessException invalid() {
    return new BusinessException(
            HttpStatus.BAD_REQUEST, "INVALID_PAYMENT_SIGNATURE", "Dữ liệu Webhook PayOS không hợp lệ.");
  }

  private BusinessException unavailable() {
    return new BusinessException(
            HttpStatus.BAD_GATEWAY,
            "PAYOS_UNAVAILABLE",
            "Không kết nối được PayOS. Vui lòng kiểm tra lại trạng thái đơn trước khi thử lại.");
  }
}