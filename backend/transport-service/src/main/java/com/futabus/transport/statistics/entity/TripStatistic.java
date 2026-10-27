package com.futabus.transport.statistics.entity;

import com.futabus.transport.trip.entity.Trip;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_statistic")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class TripStatistic {
    @Id
    @Column(name = "trip_id")
    Long tripId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    Trip trip;

    @Column(nullable = false)
    int validTicketCount;

    @Column(nullable = false, precision = 15, scale = 0)
    BigDecimal collectedAmount;

    @Column(nullable = false, precision = 15, scale = 0)
    BigDecimal refundedAmount;

    @LastModifiedDate
    LocalDateTime updatedAt;
}
