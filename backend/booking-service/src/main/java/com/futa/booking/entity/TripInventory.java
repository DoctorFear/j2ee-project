// Khóa kho ghế theo chuyến để chống giữ chỗ đồng thời
package com.futa.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trip_inventory")
@Getter
@Setter
@NoArgsConstructor
public class TripInventory {
  @Id private String tripCode;
}
