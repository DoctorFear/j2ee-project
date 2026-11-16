// Xử lý nghiệp vụ soát vé, danh sách hành khách và kiểm soát lên xe cho tài xế
package com.futa.booking.service;

import com.futa.booking.dto.driver.*;
import com.futa.booking.entity.Booking;
import com.futa.booking.entity.Ticket;
import com.futa.booking.repository.BookingRepository;
import com.futa.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverCheckInService {

    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

    /**
     * Lấy danh sách toàn bộ hành khách đã thanh toán (PAID) của chuyến
     */
    @Transactional(readOnly = true)
    public List<DriverPassengerResponse> getTripPassengers(String tripCode) {
        List<Ticket> tickets = ticketRepository.findByTripCode(tripCode);

        // Preload danh sách booking của chuyến để tránh truy vấn N+1
        List<Booking> bookings = bookingRepository.findByTripCode(tripCode);
        Map<String, Booking> bookingMap = bookings.stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity(), (a, b) -> a));

        return tickets.stream()
                .filter(t -> "PAID".equalsIgnoreCase(t.getStatus()))
                .map(t -> mapToPassengerResponse(t, bookingMap.get(t.getBookingId())))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tóm tắt tiến độ lên xe cùng danh sách hành khách
     */
    @Transactional(readOnly = true)
    public DriverTripSummaryResponse getTripSummary(String tripCode) {
        List<DriverPassengerResponse> passengers = getTripPassengers(tripCode);
        int total = passengers.size();
        int boarded = (int) passengers.stream().filter(DriverPassengerResponse::isBoarded).count();

        return DriverTripSummaryResponse.builder()
                .tripCode(tripCode)
                .totalPassengers(total)
                .totalBoarded(boarded)
                .passengers(passengers)
                .build();
    }

    /**
     * Xác thực và soát vé qua QR, mã vé hoặc 4 số cuối SĐT (Check-in)
     */
    @Transactional
    public CheckInResponse checkIn(String tripCode, CheckInRequest req, String driverId) {
        Ticket ticket = resolveTicket(tripCode, req);

        // 1. Kiểm tra vé có thuộc đúng chuyến không
        if (!ticket.getTripCode().equals(tripCode)) {
            throw new RuntimeException("Vé không thuộc chuyến này! (Mã chuyến trên vé: " + ticket.getTripCode() + ")");
        }

        // 2. Kiểm tra vé đã thanh toán chưa
        if (!"PAID".equalsIgnoreCase(ticket.getStatus())) {
            throw new RuntimeException("Vé chưa được thanh toán thành công (Trạng thái: " + ticket.getStatus() + ")");
        }

        // 3. Kiểm tra vé đã soát trước đó chưa
        if (ticket.isBoarded()) {
            throw new RuntimeException("Vé đã được soát trước đó vào lúc: " + ticket.getBoardedAt());
        }

        // 4. Cập nhật trạng thái lên xe
        ticket.setBoarded(true);
        ticket.setBoardedAt(Instant.now());
        ticket.setBoardedBy(driverId);
        ticketRepository.save(ticket);

        // Lấy thông tin booking liên kết
        Booking booking = bookingRepository.findById(ticket.getBookingId()).orElse(null);

        // Tính toán thống kê số lượng
        List<Ticket> allTickets = ticketRepository.findByTripCode(tripCode);
        int totalPassengers = (int) allTickets.stream().filter(t -> "PAID".equalsIgnoreCase(t.getStatus())).count();
        int totalBoarded = (int) allTickets.stream().filter(Ticket::isBoarded).count();

        return CheckInResponse.builder()
                .success(true)
                .message("Soát vé thành công!")
                .ticketCode(ticket.getCode())
                .seatCode(ticket.getSeatCode())
                .customerName(booking != null ? booking.getContactName() : "Hành khách")
                .customerPhone(booking != null ? booking.getContactPhone() : "")
                .pickupStationName(booking != null ? booking.getOriginId() : "Điểm đón")
                .dropOffStationName(booking != null ? booking.getDestinationId() : "Điểm trả")
                .boardedAt(ticket.getBoardedAt())
                .totalBoarded(totalBoarded)
                .totalPassengers(totalPassengers)
                .build();
    }

    /**
     * Tra cứu thông tin vé để xem trước trên màn hình soát vé
     */
    @Transactional(readOnly = true)
    public DriverPassengerResponse lookupTicket(String tripCode, String codeOrToken) {
        Ticket ticket = ticketRepository.findByQrToken(codeOrToken)
                .or(() -> ticketRepository.findById(codeOrToken))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin vé: " + codeOrToken));

        Booking booking = bookingRepository.findById(ticket.getBookingId()).orElse(null);
        return mapToPassengerResponse(ticket, booking);
    }

    /**
     * Chuyển đổi trạng thái 1 chạm (Manual Check-in trên danh sách đón trả)
     */
    @Transactional
    public DriverPassengerResponse toggleBoarded(String tripCode, String ticketCode, String driverId) {
        Ticket ticket = ticketRepository.findById(ticketCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vé: " + ticketCode));

        if (!ticket.getTripCode().equals(tripCode)) {
            throw new RuntimeException("Vé không thuộc chuyến này!");
        }

        // Đảo ngược trạng thái boarded
        boolean newState = !ticket.isBoarded();
        ticket.setBoarded(newState);
        ticket.setBoardedAt(newState ? Instant.now() : null);
        ticket.setBoardedBy(newState ? driverId : null);
        ticketRepository.save(ticket);

        Booking booking = bookingRepository.findById(ticket.getBookingId()).orElse(null);
        return mapToPassengerResponse(ticket, booking);
    }

    private Ticket resolveTicket(String tripCode, CheckInRequest req) {
        if (req.getQrToken() != null && !req.getQrToken().isBlank()) {
            return ticketRepository.findByQrToken(req.getQrToken().trim())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vé với mã QR này!"));
        }

        if (req.getTicketCode() != null && !req.getTicketCode().isBlank()) {
            return ticketRepository.findById(req.getTicketCode().trim())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mã vé: " + req.getTicketCode()));
        }

        if (req.getLast4Phone() != null && !req.getLast4Phone().isBlank()) {
            String last4 = req.getLast4Phone().trim();
            List<Booking> tripBookings = bookingRepository.findByTripCode(tripCode);
            List<String> matchingBookingIds = tripBookings.stream()
                    .filter(b -> b.getContactPhone() != null && b.getContactPhone().endsWith(last4))
                    .map(Booking::getId)
                    .toList();

            if (matchingBookingIds.isEmpty()) {
                throw new RuntimeException("Không tìm thấy khách hàng nào có 4 số cuối điện thoại: " + last4);
            }

            List<Ticket> matchingTickets = ticketRepository.findByTripCode(tripCode).stream()
                    .filter(t -> matchingBookingIds.contains(t.getBookingId()) && "PAID".equalsIgnoreCase(t.getStatus()))
                    .toList();

            if (matchingTickets.isEmpty()) {
                throw new RuntimeException("Không tìm thấy vé hợp lệ nào với 4 số cuối điện thoại: " + last4);
            }

            // Ưu tiên vé chưa lên xe
            return matchingTickets.stream()
                    .filter(t -> !t.isBoarded())
                    .findFirst()
                    .orElse(matchingTickets.get(0));
        }

        throw new RuntimeException("Vui lòng cung cấp mã QR, mã vé hoặc 4 số cuối số điện thoại để soát!");
    }

    private DriverPassengerResponse mapToPassengerResponse(Ticket t, Booking booking) {
        DriverPassengerResponse res = new DriverPassengerResponse();
        res.setTicketCode(t.getCode());
        res.setSeatCode(t.getSeatCode());

        // Xác định tầng dựa vào mã ghế (Dãy A: Tầng dưới, Dãy B: Tầng trên)
        if (t.getSeatCode() != null && t.getSeatCode().startsWith("B")) {
            res.setFloor("Tầng 2 (Dãy B)");
        } else {
            res.setFloor("Tầng 1 (Dãy A)");
        }

        if (booking != null) {
            res.setCustomerName(booking.getContactName());
            res.setCustomerPhone(booking.getContactPhone());
            res.setPickupStationId(booking.getOriginId());
            res.setDropOffStationId(booking.getDestinationId());
        } else {
            res.setCustomerName("Khách hàng");
            res.setCustomerPhone("");
        }

        res.setBoarded(t.isBoarded());
        res.setBoardedAt(t.getBoardedAt());
        res.setTicketStatus(t.getStatus());
        return res;
    }
}
