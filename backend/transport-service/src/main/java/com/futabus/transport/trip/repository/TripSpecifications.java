package com.futabus.transport.trip.repository;

import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class TripSpecifications {
    private TripSpecifications() {
    }

    public static Specification<Trip> withFilters(
            Long routeId,
            TripStatus status,
            SaleStatus saleStatus,
            LocalDate departureDate,
            LocalDateTime fromDepartureAt,
            LocalDateTime toDepartureAt
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (routeId != null) {
                predicates.add(cb.equal(root.get("route").get("id"), routeId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (saleStatus != null) {
                predicates.add(cb.equal(root.get("saleStatus"), saleStatus));
            }
            if (departureDate != null) {
                LocalDateTime startOfDay = departureDate.atStartOfDay();
                LocalDateTime endOfDay = departureDate.atTime(LocalTime.MAX);
                predicates.add(cb.between(root.get("departureAt"), startOfDay, endOfDay));
            }
            if (fromDepartureAt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("departureAt"), fromDepartureAt));
            }
            if (toDepartureAt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("departureAt"), toDepartureAt));
            }

            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

