package com.futabus.transport.route.entity;

import com.futabus.transport.route.enums.RouteStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "routes",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"code", "version_no"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "code", nullable = false)
    String code;

    @Column(name = "version_no", nullable = false)
    int versionNo;

    @Column(nullable = false)
    String name;

    @Column(nullable = false, precision = 15, scale = 0)
    BigDecimal basePrice;

    @Column(nullable = false)
    LocalDateTime effectiveFromAt;

    LocalDateTime effectiveToAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    RouteStatus status;
}
