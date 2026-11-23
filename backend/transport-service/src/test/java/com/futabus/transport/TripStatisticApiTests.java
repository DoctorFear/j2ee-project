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
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TripStatisticApiTests {
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Autowired
    WebApplicationContext context;

    @Autowired
    TripStatisticRepository tripStatisticRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    MockMvc mvc;
    Trip trip;
    Route route;
    Vehicle vehicle;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        route = routeRepository.save(Route.builder().code("STATISTIC-TEST-ROUTE").versionNo(1).name("HCM - Da Lat")
                .basePrice(new BigDecimal("250000")).effectiveFromAt(LocalDateTime.of(2026, 10, 22, 0, 0))
                .status(RouteStatus.INACTIVE).build());
        vehicle = vehicleRepository.save(Vehicle.builder().licensePlate("51B-67890").type(ServiceType.SEAT).capacity(45)
                .status(VehicleStatus.INACTIVE).build());
        trip = tripRepository.save(Trip.builder().code("STATISTIC-TEST-TRIP").route(route).vehicle(vehicle)
                .departureAt(LocalDateTime.of(2026, 10, 23, 8, 0)).arrivalAt(LocalDateTime.of(2026, 10, 23, 14, 0))
                .ticketPrice(new BigDecimal("250000")).capacitySnapshot(45).status(TripStatus.DRAFT)
                .saleStatus(SaleStatus.NOT_OPEN).build());
    }

    @AfterEach
    void cleanUp() {
        tripStatisticRepository.deleteById(trip.getId());
        tripRepository.deleteById(trip.getId());
        routeRepository.deleteById(route.getId());
        vehicleRepository.deleteById(vehicle.getId());
    }

    @Test
    void createAndReadUseTripIdAndPreserveTrip() throws Exception {
        mvc.perform(get("/trip-statistics")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());

        String location = createStatistic();
        assertEquals("/trip-statistics/" + trip.getId(), location);
        assertTrue(tripStatisticRepository.existsById(trip.getId()));

        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(trip.getId()))
                .andExpect(jsonPath("$.data.validTicketCount").value(10))
                .andExpect(jsonPath("$.data.collectedAmount").value(2500000))
                .andExpect(jsonPath("$.data.refundedAmount").value(0))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty()).andExpect(jsonPath("$.data.trip").doesNotExist());
        mvc.perform(get("/trip-statistics")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].tripId").value(trip.getId()));

        mvc.perform(delete(location)).andExpect(status().isMethodNotAllowed());
        assertTrue(tripStatisticRepository.existsById(trip.getId()));
        assertTrue(tripRepository.existsById(trip.getId()));
    }

    @Test
    void acceptsOnlyNewerRevisionsAndReplacesTheEntireSnapshot() throws Exception {
        String location = createStatistic();
        String revisionTwo = body(trip.getId()).replace("\"sourceRevision\":1", "\"sourceRevision\":2")
                .replace("\"validTicketCount\":10", "\"validTicketCount\":4").replace("2500000", "1000000")
                .replace("\"refundedAmount\":0", "\"refundedAmount\":250000");
        LocalDateTime beforeUpdate = LocalDateTime.now(VIETNAM_ZONE).truncatedTo(ChronoUnit.MICROS);
        MvcResult updated = mvc.perform(post("/trip-statistics").contentType(MediaType.APPLICATION_JSON)
                        .content(revisionTwo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.sourceRevision").value(2))
                .andExpect(jsonPath("$.data.refundedAmount").value(250000))
                .andReturn();
        LocalDateTime acceptedAt = assertAuditTimestamp(updated, beforeUpdate);
        for (String stale : List.of(body(trip.getId()), revisionTwo.replace("1000000", "999"))) {
            mvc.perform(post("/trip-statistics").contentType(MediaType.APPLICATION_JSON).content(stale))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.data.sourceRevision").value(2))
                    .andExpect(jsonPath("$.data.collectedAmount").value(1000000));
        }
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.data.validTicketCount").value(4));
        assertEquals(acceptedAt, tripStatisticRepository.findById(trip.getId()).orElseThrow().getUpdatedAt());
        assertEquals(1, tripStatisticRepository.count());
    }

    @Test
    void invalidInputIsRejectedWithoutSaving() throws Exception {
        String valid = body(trip.getId());
        for (String invalid : List.of("{}", valid.replace("\"tripId\":" + trip.getId(), "\"tripId\":0"),
                valid.replace("\"validTicketCount\":10", "\"validTicketCount\":null"),
                valid.replace("\"validTicketCount\":10", "\"validTicketCount\":-1"), valid.replace("2500000", "null"),
                valid.replace("2500000", "-1"), valid.replace("2500000", "1.5"),
                valid.replace("2500000", "1000000000000000"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":null"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":-1"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":1.5"),
                valid.replace("\"refundedAmount\":0", "\"refundedAmount\":1000000000000000"))) {
            mvc.perform(post("/trip-statistics").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(0, tripStatisticRepository.count());
    }

    @Test
    void missingResourcesReturnNotFoundAndUpdatesAreNotSupported() throws Exception {
        mvc.perform(post("/trip-statistics").contentType(MediaType.APPLICATION_JSON).content(body(999999L)))
                .andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/trip-statistics/999999")).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(delete("/trip-statistics/999999")).andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(put("/trip-statistics/{tripId}", trip.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(body(trip.getId()))).andExpect(status().isMethodNotAllowed());
        mvc.perform(patch("/trip-statistics/{tripId}", trip.getId()).contentType(MediaType.APPLICATION_JSON)
                .content("{}")).andExpect(status().isMethodNotAllowed());
        assertEquals(0, tripStatisticRepository.count());
    }

    private String createStatistic() throws Exception {
        LocalDateTime beforeCreate = LocalDateTime.now(VIETNAM_ZONE).truncatedTo(ChronoUnit.MICROS);
        MvcResult created = mvc.perform(post("/trip-statistics").contentType(MediaType.APPLICATION_JSON)
                        .content(body(trip.getId())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/trip-statistics/" + trip.getId()))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.tripId").value(trip.getId()))
                .andReturn();
        assertAuditTimestamp(created, beforeCreate);
        return created.getResponse().getHeader("Location");
    }

    private LocalDateTime assertAuditTimestamp(MvcResult result, LocalDateTime beforeRequest) throws Exception {
        LocalDateTime afterRequest = LocalDateTime.now(VIETNAM_ZONE);
        String responseTimestamp = JsonPath.read(result.getResponse().getContentAsString(), "$.data.updatedAt");
        LocalDateTime updatedAt = LocalDateTime.parse(responseTimestamp);

        assertFalse(updatedAt.isBefore(beforeRequest));
        assertFalse(updatedAt.isAfter(afterRequest));
        assertEquals(updatedAt, tripStatisticRepository.findById(trip.getId()).orElseThrow().getUpdatedAt());
        return updatedAt;
    }

    private String body(Long tripId) {
        return """
                {"tripId":%d,"validTicketCount":10,"collectedAmount":2500000,"refundedAmount":0,
                 "sourceRevision":1,"sourceAsOfAt":"2026-10-23T08:00:00"}
                """.formatted(tripId);
    }
}
