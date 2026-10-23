package com.futabus.transport;

import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.trip.repository.TripDriverRepository;
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
class TripApiTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    TripRepository trips;

    @Autowired
    TripDriverRepository tripDrivers;

    @Autowired
    RouteRepository routes;

    @Autowired
    VehicleRepository vehicles;

    @Autowired
    DriverRepository drivers;

    MockMvc mvc;
    Route route;
    Vehicle vehicle;
    Driver driver;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        route = routes.save(Route.builder().code("TRIP-TEST-ROUTE").versionNo(1).name("HCM - Da Lat")
                .basePrice(new BigDecimal("250000"))
                .effectiveFromAt(LocalDateTime.of(2026, 10, 22, 0, 0))
                .status(RouteStatus.INACTIVE).build());
        vehicle = vehicles.save(Vehicle.builder().licensePlate("51B-12345").type(ServiceType.SEAT)
                .capacity(45).status(VehicleStatus.INACTIVE).build());
        driver = drivers.save(Driver.builder().fullName("Nguyen Van A").phone("0901234567")
                .status(DriverStatus.INACTIVE).build());
    }

    @AfterEach
    void cleanUp() {
        tripDrivers.deleteAll();
        trips.deleteAll();
        routes.deleteById(route.getId());
        vehicles.deleteById(vehicle.getId());
        drivers.deleteById(driver.getId());
    }

    @Test
    void tripCreateReadAndDeleteFollowStationContract() throws Exception {
        mvc.perform(get("/api/trips")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        String location = createTrip();
        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("HCM-DL-001"))
                .andExpect(jsonPath("$.data.routeId").value(route.getId()))
                .andExpect(jsonPath("$.data.vehicleId").value(vehicle.getId()))
                .andExpect(jsonPath("$.data.ticketPrice").value(250000))
                .andExpect(jsonPath("$.data.departureAt").value("2026-10-23T08:00:00"))
                .andExpect(jsonPath("$.data.arrivalAt").value("2026-10-23T14:00:00"))
                .andExpect(jsonPath("$.data.route").doesNotExist())
                .andExpect(jsonPath("$.data.vehicle").doesNotExist());

        vehicle.setCapacity(30);
        vehicles.save(vehicle);
        mvc.perform(get(location)).andExpect(jsonPath("$.data.capacitySnapshot").value(45));
        mvc.perform(get("/api/trips")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(delete(location)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(delete(location)).andExpect(status().isNotFound());
        assertEquals(0, trips.count());
        assertTrue(routes.existsById(route.getId()));
        assertTrue(vehicles.existsById(vehicle.getId()));
    }

    @Test
    void tripDriverCreateReadAndDeletePreserveParentsAndRejectDuplicates() throws Exception {
        Long tripId = idFromLocation(createTrip());
        mvc.perform(get("/api/trip-drivers")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        String body = assignmentBody(tripId, driver.getId());
        String location = mvc.perform(post("/api/trip-drivers")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(content().json("{\"data\":" + body + "}"))
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        assertEquals("/api/trip-drivers/" + idFromLocation(location), location);

        mvc.perform(get(location)).andExpect(status().isOk())
                .andExpect(content().json("{\"data\":" + body + "}"));
        mvc.perform(get("/api/trip-drivers")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(post("/api/trip-drivers").contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("PRIMARY", "RELIEF")))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.success").value(false));
        mvc.perform(delete("/api/trips/{id}", tripId)).andExpect(status().isConflict());
        assertEquals(1, tripDrivers.count());

        mvc.perform(delete(location)).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(delete(location)).andExpect(status().isNotFound());
        assertTrue(trips.existsById(tripId));
        assertTrue(drivers.existsById(driver.getId()));
        mvc.perform(delete("/api/trips/{id}", tripId)).andExpect(status().isNoContent());
    }

    @Test
    void invalidTripBodiesAreRejectedWithoutSaving() throws Exception {
        String valid = tripBody(route.getId(), vehicle.getId());
        for (String invalid : List.of(
                "{}",
                valid.replace("HCM-DL-001", " "),
                valid.replace("HCM-DL-001", "x".repeat(256)),
                valid.replace("\"routeId\":" + route.getId(), "\"routeId\":0"),
                valid.replace("\"vehicleId\":" + vehicle.getId(), "\"vehicleId\":null"),
                valid.replace("250000", "-1"),
                valid.replace("250000", "1.5"),
                valid.replace("250000", "1000000000000000"),
                valid.replace("2026-10-23T08:00:00", "invalid"),
                valid.replace("\"2026-10-23T08:00:00\"", "null"),
                valid.replace("\"2026-10-23T14:00:00\"", "null"),
                valid.replace("2026-10-23T14:00:00", "2026-10-23T08:00:00"),
                valid.replace("2026-10-23T14:00:00", "2026-10-23T07:00:00"))) {
            mvc.perform(post("/api/trips").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
        assertEquals(0, trips.count());
    }

    @Test
    void invalidTripDriverBodiesAreRejectedWithoutSaving() throws Exception {
        Long tripId = idFromLocation(createTrip());
        String valid = assignmentBody(tripId, driver.getId());
        for (String invalid : List.of(
                "{}",
                valid.replace("\"tripId\":" + tripId, "\"tripId\":0"),
                valid.replace("\"driverId\":" + driver.getId(), "\"driverId\":null"),
                valid.replace("\"role\":\"PRIMARY\"", "\"role\":null"),
                valid.replace("PRIMARY", "UNKNOWN"))) {
            mvc.perform(post("/api/trip-drivers").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
        assertEquals(0, tripDrivers.count());
    }

    @Test
    void missingReferencesAndResourcesReturnNotFound() throws Exception {
        for (String body : List.of(tripBody(999999L, vehicle.getId()), tripBody(route.getId(), 999999L))) {
            mvc.perform(post("/api/trips").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
        }
        Long tripId = idFromLocation(createTrip());
        for (String body : List.of(assignmentBody(999999L, driver.getId()), assignmentBody(tripId, 999999L))) {
            mvc.perform(post("/api/trip-drivers").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value(false));
        }
        for (String resource : List.of("trips", "trip-drivers")) {
            mvc.perform(get("/api/" + resource + "/999999")).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
            mvc.perform(delete("/api/" + resource + "/999999")).andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
        assertEquals(0, tripDrivers.count());
    }

    @Test
    void updatesAreNotSupported() throws Exception {
        for (String resource : List.of("trips", "trip-drivers")) {
            mvc.perform(put("/api/" + resource + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isMethodNotAllowed());
            mvc.perform(patch("/api/" + resource + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    private String createTrip() throws Exception {
        String location = mvc.perform(post("/api/trips").contentType(MediaType.APPLICATION_JSON)
                        .content(tripBody(route.getId(), vehicle.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.saleStatus").value("NOT_OPEN"))
                .andExpect(jsonPath("$.data.capacitySnapshot").value(45))
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        assertEquals("/api/trips/" + idFromLocation(location), location);
        return location;
    }

    private Long idFromLocation(String location) {
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private String tripBody(Long routeId, Long vehicleId) {
        return """
                {"code":"HCM-DL-001","routeId":%d,"vehicleId":%d,
                 "departureAt":"2026-10-23T08:00:00","arrivalAt":"2026-10-23T14:00:00","ticketPrice":250000}
                """.formatted(routeId, vehicleId);
    }

    private String assignmentBody(Long tripId, Long driverId) {
        return """
                {"tripId":%d,"driverId":%d,"role":"PRIMARY"}
                """.formatted(tripId, driverId);
    }
}
