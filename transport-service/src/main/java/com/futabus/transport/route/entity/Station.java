package com.futabus.transport.route.entity;

import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.enums.StationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "stations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StationStatus status;
}
