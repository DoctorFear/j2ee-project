// Gửi vé điện tử bằng Spring Email và xử lý gửi lại
package com.futa.booking.service;

import com.futa.booking.config.BookingProperties;
import com.futa.booking.repository.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailService {
  private final JavaMailSender sender;
  private final BookingProperties properties;
  private final BookingRepository bookings;
  private final TicketRepository tickets;
  private final MailOutboxRepository outbox;
  private final tools.jackson.databind.ObjectMapper mapper;

  // Chỉ gửi vé đã thanh toán và lưu lịch thử lại nếu SMTP thất bại
  @Transactional
  public void deliverPending() {
    for (var job :
        outbox.findTop20BySentAtIsNullAndNextAttemptAtBeforeOrderByNextAttemptAt(Instant.now())) {
      try {
        var booking = bookings.findById(job.getBookingId()).orElseThrow();
        var journey =
            mapper.readValue(booking.getTripSnapshot(), com.futa.booking.dto.Journey.class);
        var validTickets =
            tickets.findByBookingId(booking.getId()).stream()
                .filter(ticket -> ticket.getStatus().equals("PAID"))
                .toList();
        if (validTickets.isEmpty()) {
          job.setSentAt(Instant.now());
          continue;
        }
        var mime = sender.createMimeMessage();
        var message = new MimeMessageHelper(mime, true, "UTF-8");
        message.setFrom(properties.getEmailFrom());
        message.setTo(booking.getContactEmail());
        message.setSubject("Vé điện tử FUTA - " + booking.getTripCode());
        StringBuilder text =
            new StringBuilder(
                "Xin chào "
                    + booking.getContactName()
                    + ",\nĐặt vé thành công.\nChuyến: "
                    + booking.getTripCode()
                    + "\nTuyến: "
                    + journey.routeName()
                    + "\nĐiểm đón: "
                    + journey.stations().stream()
                        .filter(s -> s.id().equals(booking.getOriginId()))
                        .map(s -> s.name() + " - " + s.address())
                        .findFirst()
                        .orElse(booking.getOriginId())
                    + "\nĐiểm trả: "
                    + journey.stations().stream()
                        .filter(s -> s.id().equals(booking.getDestinationId()))
                        .map(s -> s.name() + " - " + s.address())
                        .findFirst()
                        .orElse(booking.getDestinationId())
                    + "\nGiờ đón: "
                    + java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")
                        .withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                        .format(booking.getPickupAt())
                    + "\n");
        for (var ticket : validTickets) {
          text.append("Mã vé: ")
              .append(ticket.getCode())
              .append(" - Ghế: ")
              .append(ticket.getSeatCode())
              .append('\n');
          var png = new ByteArrayOutputStream();
          MatrixToImageWriter.writeToStream(
              new QRCodeWriter().encode(ticket.getQrToken(), BarcodeFormat.QR_CODE, 320, 320),
              "PNG",
              png);
          message.addAttachment(
              ticket.getCode() + ".png", new ByteArrayResource(png.toByteArray()), "image/png");
        }
        text.append("Vui lòng có mặt trước giờ đón 15 phút. Xem và xuất trình QR vé tại: ")
            .append(properties.getFrontendUrl())
            .append("/my-tickets");
        message.setText(text.toString());
        sender.send(mime);
        job.setSentAt(Instant.now());
      } catch (Exception error) {
        job.setAttempts(job.getAttempts() + 1);
        job.setNextAttemptAt(
            Instant.now()
                .plusSeconds(Math.min(3600, 60L * (1L << Math.min(job.getAttempts(), 5)))));
      }
    }
  }
}
