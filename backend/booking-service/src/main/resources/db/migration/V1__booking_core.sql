-- Cấu trúc đơn, ghế, thanh toán, vé và hàng đợi email
CREATE TABLE trip_inventory (trip_code VARCHAR(255) PRIMARY KEY);
CREATE TABLE bookings (
 id VARCHAR(255) PRIMARY KEY, user_id VARCHAR(255) NOT NULL, trip_code VARCHAR(255) NOT NULL,
 origin_id VARCHAR(255) NOT NULL, destination_id VARCHAR(255) NOT NULL,
 contact_name VARCHAR(255) NOT NULL, contact_phone VARCHAR(255) NOT NULL, contact_email VARCHAR(255) NOT NULL,
 fare DECIMAL(15,0) NOT NULL, amount DECIMAL(15,0) NOT NULL, status VARCHAR(255) NOT NULL,
 expires_at DATETIME(6) NOT NULL, created_at DATETIME(6) NOT NULL,
 pickup_at DATETIME(6) NOT NULL, arrival_at DATETIME(6) NOT NULL,
 trip_snapshot LONGTEXT NOT NULL, seat_codes VARCHAR(255) NOT NULL,
 INDEX idx_booking_expiry(status,expires_at), INDEX idx_booking_user(user_id)
);
CREATE TABLE seat_reservations (
 id BIGINT AUTO_INCREMENT PRIMARY KEY, trip_code VARCHAR(255) NOT NULL, seat_code VARCHAR(255) NOT NULL,
 booking_id VARCHAR(255) NOT NULL, status VARCHAR(255) NOT NULL, expires_at DATETIME(6) NOT NULL,
 CONSTRAINT uq_trip_seat UNIQUE(trip_code,seat_code),
 FOREIGN KEY(booking_id) REFERENCES bookings(id), INDEX idx_seat_expiry(status,expires_at)
);
CREATE TABLE payments (
 id VARCHAR(255) PRIMARY KEY, booking_id VARCHAR(255) NOT NULL UNIQUE, request_id VARCHAR(255) NOT NULL,
 amount DECIMAL(15,0) NOT NULL, status VARCHAR(255) NOT NULL, provider_txn_id VARCHAR(255) UNIQUE,
 pay_url VARCHAR(4000), qr_code_url VARCHAR(4000), completed_at DATETIME(6),
 FOREIGN KEY(booking_id) REFERENCES bookings(id)
);
CREATE TABLE tickets (
 code VARCHAR(255) PRIMARY KEY, booking_id VARCHAR(255) NOT NULL, user_id VARCHAR(255) NOT NULL,
 trip_code VARCHAR(255) NOT NULL, seat_code VARCHAR(255) NOT NULL, qr_token VARCHAR(255) NOT NULL UNIQUE,
 status VARCHAR(255) NOT NULL, fare DECIMAL(15,0) NOT NULL, issued_at DATETIME(6) NOT NULL,
 UNIQUE(booking_id,seat_code), FOREIGN KEY(booking_id) REFERENCES bookings(id), INDEX idx_ticket_user(user_id)
);
CREATE TABLE mail_outbox (
 booking_id VARCHAR(255) PRIMARY KEY, attempts INT NOT NULL, next_attempt_at DATETIME(6) NOT NULL, sent_at DATETIME(6),
 FOREIGN KEY(booking_id) REFERENCES bookings(id)
);
