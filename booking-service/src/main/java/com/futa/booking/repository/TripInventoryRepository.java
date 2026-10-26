// Khóa dữ liệu tồn ghế của chuyến trong giao dịch
package com.futa.booking.repository;

import com.futa.booking.entity.TripInventory;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface TripInventoryRepository extends JpaRepository<TripInventory, String> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from TripInventory i where i.tripCode=:code")
  Optional<TripInventory> lock(@Param("code") String code);
}
