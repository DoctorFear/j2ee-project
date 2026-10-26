-- Lưu yêu cầu hoàn tiền và chống gửi trùng
CREATE TABLE refunds (
 ticket_code VARCHAR(255) PRIMARY KEY, status VARCHAR(255) NOT NULL,
 requested_at DATETIME(6) NOT NULL, next_attempt_at DATETIME(6) NOT NULL, attempts INT NOT NULL,
 provider_txn_id VARCHAR(255), FOREIGN KEY(ticket_code) REFERENCES tickets(code),
 INDEX idx_refund_pending(status,next_attempt_at)
);
