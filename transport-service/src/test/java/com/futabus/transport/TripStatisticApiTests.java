package com.futabus.transport;

import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.statistics.repository.TripStatisticRepository;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.enums.SaleStatus;
import com.futabus.transport.trip.enums.TripStatus;
import com.futabus.transport.trip.repository.TripRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TripStatisticApiTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    TripStatisticRepository statistics;

    @Autowired
    TripRepository trips;

    @Autowired
    RouteRepository routes;

    @Autowired
    VehicleRepository vehicles;

    MockMvc mvc;
    Trip trip;
    Route route;
    Vehicle vehicle;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        route = routes.save(Route.builder().code("STATISTIC-TEST-ROUTE").versionNo(1).name("HCM - Da Lat")
                .basePrice(new BigDecimal("250000"))
                .effectiveFromAt(LocalDateTime.of(2026, 10, 22, 0, 0))
                .status(RouteStatus.INACTIVE).build());
        vehicle = vehicles.save(Vehicle.builder().licensePlate("51B-67890").type(ServiceType.SEAT)
                .capacity(45).status(VehicleStatus.INACTIVE).build());
        trip = trips.save(Trip.builder().code("STATISTIC-TEST-TRIP").route(route).vehicle(vehicle)
                .departureAt(LocalDateTime.of(2026, 10, 23, 8, 0))
                .arrivalAt(LocalDateTime.of(2026, 10, 23, 14, 0))
                .ticketPrice(new BigDecimal("250000")).capacitySnapshot(45)
                .status(TripStatus.DRAFT).saleStatus(SaleStatus.NOT_OPEN).build());
    }

    @AfterEach
    void cleanUp() {
        statistics.deleteById(trip.getId());
        trips.deleteById(trip.getId());
        routes.deleteById(route.getId());
        vehicles.deleteById(vehicle.getId());
    }

    @Test
    void createReadAndDeleteUseTripIdAndPreserveTrip() throws Exception {
        mvc.perform(get("/api/trip-statistics")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        String location = createStatistic();
        assertEquals("/api/trip-statistics/" + trip.getId(), location);
        assertTrue(statistics.existsById(trip.getId()));

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(trip.getId()))
                .andExpect(jsonPath("$.data.validTicketCount").value(10))
                .andExpect(jsonPath("$.data.collectedAmount").value(2500000))
                .andExpect(jsonPath("$.data.refundedAmount").value(0))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andExpect(jsonPath("$.data.trip").doesNotExist());
        mvc.perform(get("/api/trip-statistics")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].tripId").value(trip.getId()));

        mvc.perform(delete(location)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        assertFalse(statistics.existsById(trip.getId()));
        assertTrue(trips.existsById(trip.getId()));
        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(delete(location)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void duplicateCreateDoesNotOverwriteExistingStatistic() throws Exception {
        String location = createStatistic();
        mvc.perform(post("/api/trip-statistics").contentType(MediaType.APPLICATION_JSON)
                        .content(body(trip.getId()).replace("2500000", "999")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.collectedAmount").value(2500000));
        assertEquals(1, statistics.count());
    }

    @Test
    void invalidInputIsRejectedWithoutSaving() throws Exception {
        String valid = body(trip.getId());
        for (String invalid : List.of(
                "{}",
                valid.replace("\"tripId\":" + trip.getId(), "\"tripId\":0"),
                valid.replace("\"validTicketCount\":10", "\"validTicketCount\":null"),
                valid.replace("\"validTicketCount\":10", "\"validTicketCount\":-1"),
                valid.replace("2500000", "null"),
                valid.replace("2500000", "-1"),
                valid.replace("2500000", "1.5"),
                valid.replace("2500000", "1000000000000000"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":null"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":-1"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":1.5"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":1000000000000000"))) {
            mvc.perform(post("/api/trip-statistics").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
        assertEquals(0, statistics.count());
    }

    @Test
    void missingResourcesReturnNotFoundAndUpdatesAreNotSupported() throws Exception {
        mvc.perform(post("/api/trip-statistics").contentType(MediaType.APPLICATION_JSON)
                        .content(body(999999L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get("/api/trip-statistics/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(delete("/api/trip-statistics/999999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(put("/api/trip-statistics/{tripId}", trip.getId())
                        .contentType(MediaType.APPLICATION_JSON).content(body(trip.getId())))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(patch("/api/trip-statistics/{tripId}", trip.getId())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
        assertEquals(0, statistics.count());
    }

    private String createStatistic() throws Exception {
        return mvc.perform(post("/api/trip-statistics").contentType(MediaType.APPLICATION_JSON)
                        .content(body(trip.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/trip-statistics/" + trip.getId()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(trip.getId()))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty())
                .andReturn().getResponse().getHeader("Location");
    }

    private String body(Long tripId) {
        return """
                {"tripId":%d,"validTicketCount":10,"collectedAmount":2500000,"refundedAmount":0}
                """.formatted(tripId);
    }
}
