package com.futabus.transport.route.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "station_addresses",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_station_address_effective_from",
        columnNames = { "station_id", "effective_from_at" }
    )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    Station station;

    @Column(nullable = false, length = 255)
    String address;

    @Column(nullable = false)
    LocalDateTime effectiveFromAt;
}
