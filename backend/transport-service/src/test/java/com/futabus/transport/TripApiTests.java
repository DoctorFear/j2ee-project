package com.futabus.transport;

import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.entity.RouteStop;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.enums.StationType;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.route.repository.StationRepository;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.trip.repository.TripDriverRepository;
import com.futabus.transport.trip.repository.TripRepository;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.enums.SaleStatus;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    TripRepository tripRepository;

    @Autowired
    TripDriverRepository tripDriverRepository;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    RouteStopRepository routeStopRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    DriverRepository driverRepository;

    MockMvc mvc;
    Route route;
    Vehicle vehicle;
    Driver driver;
    Station origin;
    Station destination;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        route = routeRepository.save(Route.builder().code("TRIP-TEST-ROUTE").versionNo(1).name("HCM - Da Lat")
                .basePrice(new BigDecimal("250000")).effectiveFromAt(LocalDateTime.of(2026, 10, 22, 0, 0))
                .status(RouteStatus.ACTIVE).build());
        origin = stationRepository.save(Station.builder()
                .name("Origin")
                .type(StationType.TERMINAL)
                .status(StationStatus.ACTIVE)
                .build());
        destination = stationRepository.save(Station.builder()
                .name("Destination")
                .type(StationType.TERMINAL)
                .status(StationStatus.ACTIVE)
                .build());
        routeStopRepository.save(RouteStop.builder()
                .route(route)
                .station(origin)
                .stopOrder(1)
                .distanceKm(BigDecimal.ZERO)
                .allowPickup(true)
                .build());
        routeStopRepository.save(RouteStop.builder()
                .route(route)
                .station(destination)
                .stopOrder(2)
                .distanceKm(BigDecimal.valueOf(300))
                .offsetMinutes(360)
                .allowDropOff(true)
                .build());
        vehicle = vehicleRepository.save(Vehicle.builder()
                .licensePlate("51B-12345")
                .type(ServiceType.SEAT)
                .capacity(45)
                .status(VehicleStatus.ACTIVE)
                .build());
        driver = driverRepository.save(Driver.builder()
                .fullName("Nguyen Van A")
                .phone("0901234567")
                .status(DriverStatus.ACTIVE)
                .build());
    }

    @AfterEach
    void cleanUp() {
        tripDriverRepository.deleteAll();
        tripRepository.deleteAll();
        routeStopRepository.deleteAll();
        routeRepository.deleteById(route.getId());
        stationRepository.deleteAll(List.of(origin, destination));
        vehicleRepository.deleteById(vehicle.getId());
        driverRepository.deleteById(driver.getId());
    }

    @Test
    void tripCreateReadAndDeleteFollowStationContract() throws Exception {
        mvc.perform(get("/trips")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());

        String location = createTrip();
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.code").value("HCM-DL-001"))
                .andExpect(jsonPath("$.data.routeId").value(route.getId()))
                .andExpect(jsonPath("$.data.vehicleId").value(vehicle.getId()))
                .andExpect(jsonPath("$.data.ticketPrice").value(250000))
                .andExpect(jsonPath("$.data.departureAt").value("2026-10-23T08:00:00"))
                .andExpect(jsonPath("$.data.arrivalAt").value("2026-10-23T14:00:00"))
                .andExpect(jsonPath("$.data.route").doesNotExist())
                .andExpect(jsonPath("$.data.vehicle").doesNotExist());

        vehicle.setCapacity(30);
        vehicleRepository.save(vehicle);
        mvc.perform(get(location)).andExpect(jsonPath("$.data.capacitySnapshot").value(45));
        mvc.perform(get("/trips")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_NOT_FOUND"));
        mvc.perform(delete(location)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_NOT_FOUND"));
        assertEquals(0, tripRepository.count());
        assertTrue(routeRepository.existsById(route.getId()));
        assertTrue(vehicleRepository.existsById(vehicle.getId()));
    }

    @Test
    void tripDriverCreateReadAndDeletePreserveParentsAndRejectDuplicates() throws Exception {
        Long tripId = idFromLocation(createTrip());
        mvc.perform(get("/trip-drivers")).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        String body = assignmentBody(tripId, driver.getId());
        String location = mvc.perform(post("/trip-drivers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber()).andExpect(content().json("{\"data\":" + body + "}"))
                .andReturn().getResponse().getHeader("Location");
        assertNotNull(location);
        assertEquals("/trip-drivers/" + idFromLocation(location), location);

        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(content().json("{\"data\":" + body + "}"));
        mvc.perform(get("/trip-drivers")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(post("/trip-drivers").contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("PRIMARY", "RELIEF"))).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_DRIVER_ALREADY_ASSIGNED"));
        mvc.perform(delete("/trips/{id}", tripId)).andExpect(status().isConflict());
        assertEquals(1, tripDriverRepository.count());

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        mvc.perform(get(location)).andExpect(status().isNotFound());
        mvc.perform(delete(location)).andExpect(status().isNotFound());
        assertTrue(tripRepository.existsById(tripId));
        assertTrue(driverRepository.existsById(driver.getId()));
        mvc.perform(delete("/trips/{id}", tripId)).andExpect(status().isNoContent());
    }

    @Test
    void driverTripStatusErrorsUseTheGlobalHandler() throws Exception {
        String path = "/driver/trips/{code}/status";
        mvc.perform(put(path, "missing").param("status", "RUNNING")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_CODE_NOT_FOUND"));

        createTrip();
        mvc.perform(put(path, "HCM-DL-001").param("status", "unknown")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_INVALID_STATUS"));
        mvc.perform(put(path, "HCM-DL-001")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_MISSING_PARAMETER"))
                .andExpect(jsonPath("$.errors.status[0]").isString());

        mvc.perform(put(path, "HCM-DL-001").param("status", "RUNNING")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_TRANSITION_INVALID"));
    }

    @Test
    void draftWithoutVehicleCanPublishAndRunWithoutChangingPlannedTimes() throws Exception {
        ZoneId vietnam = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime departure = LocalDate.now(vietnam).plusDays(1).atTime(23, 30);
        String body = tripBody(route.getId(), vehicle.getId())
                .replace("\"vehicleId\":" + vehicle.getId(), "\"vehicleId\":null")
                .replace("2026-10-23T08:00:00", departure.toString())
                .replace("2026-10-23T14:00:00", departure.plusHours(6).toString());
        String location = mvc.perform(post("/trips").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.capacitySnapshot").isEmpty()).andReturn()
                .getResponse().getHeader("Location");
        Long tripId = idFromLocation(location);

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(body.replace("250000", "260000")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.ticketPrice").value(260000));
        mvc.perform(post(location + "/publish")).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_VEHICLE_REQUIRED"));
        mvc.perform(put(location + "/vehicle").contentType(MediaType.APPLICATION_JSON)
                .content("{\"vehicleId\":" + vehicle.getId() + "}")).andExpect(status().isOk());
        mvc.perform(post("/trip-drivers").contentType(MediaType.APPLICATION_JSON)
                .content(assignmentBody(tripId, driver.getId()))).andExpect(status().isCreated());
        mvc.perform(post(location + "/publish")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.publishedAt").isString());
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(put(location + "/sale-status").contentType(MediaType.APPLICATION_JSON)
                .content("{\"saleStatus\":\"OPEN\"}")).andExpect(status().isOk());
        mvc.perform(delete("/route-stops/{id}", routeStopRepository.findAll().getFirst().getId()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.errorCode").value("TRANSPORT_ROUTE_PINNED"));

        mvc.perform(get("/driver/trips").param("driverId", driver.getId().toString()).param("date",
                departure.toLocalDate().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].tripCode").value("HCM-DL-001"));
        mvc.perform(get("/driver/trips").param("driverId", driver.getId().toString()).param("date",
                departure.toLocalDate().plusDays(1).toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        LocalDateTime beforeDeparture = LocalDateTime.now(vietnam).minusSeconds(1);
        String driverStatus = "/driver/trips/HCM-DL-001/status";
        mvc.perform(put(driverStatus).param("status", "RUNNING")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tripStatus").value("RUNNING"))
                .andExpect(jsonPath("$.data.actualDepartureAt").isString());
        mvc.perform(put(driverStatus).param("status", "COMPLETED")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tripStatus").value("COMPLETED"));
        Trip completed = tripRepository.findById(tripId).orElseThrow();
        assertEquals(departure, completed.getDepartureAt());
        assertEquals(SaleStatus.CLOSED, completed.getSaleStatus());
        assertFalse(completed.getActualDepartureAt().isBefore(beforeDeparture));
        assertFalse(completed.getActualArrivalAt().isAfter(LocalDateTime.now(vietnam).plusSeconds(1)));
        assertTrue(completed.getActualArrivalAt().isAfter(completed.getActualDepartureAt()));
    }

    @Test
    void invalidTripBodiesAreRejectedWithoutSaving() throws Exception {
        String valid = tripBody(route.getId(), vehicle.getId());
        for (String invalid : List.of("{}", valid.replace("HCM-DL-001", " "),
                valid.replace("HCM-DL-001", "x".repeat(256)),
                valid.replace("\"routeId\":" + route.getId(), "\"routeId\":0"),
                valid.replace("\"vehicleId\":" + vehicle.getId(), "\"vehicleId\":0"), valid.replace("250000", "-1"),
                valid.replace("250000", "1.5"), valid.replace("250000", "1000000000000000"),
                valid.replace("2026-10-23T08:00:00", "invalid"), valid.replace("\"2026-10-23T08:00:00\"", "null"),
                valid.replace("\"2026-10-23T14:00:00\"", "null"),
                valid.replace("2026-10-23T14:00:00", "2026-10-23T08:00:00"),
                valid.replace("2026-10-23T14:00:00", "2026-10-23T07:00:00"))) {
            mvc.perform(post("/trips").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(0, tripRepository.count());
    }

    @Test
    void inactiveDriverCannotBeAssignedToTrip() throws Exception {
        Driver inactiveDriver = driverRepository.save(Driver.builder()
                .fullName("Inactive Driver")
                .phone("0909999999")
                .status(DriverStatus.INACTIVE)
                .build());
        Long tripId = idFromLocation(createTrip());

        mvc.perform(post("/trip-drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignmentBody(tripId, inactiveDriver.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_TRIP_DRIVER_INELIGIBLE"));

        driverRepository.deleteById(inactiveDriver.getId());
    }

    @Test
    void invalidTripDriverBodiesAreRejectedWithoutSaving() throws Exception {
        Long tripId = idFromLocation(createTrip());
        String valid = assignmentBody(tripId, driver.getId());
        for (String invalid : List.of("{}", valid.replace("\"tripId\":" + tripId, "\"tripId\":0"),
                valid.replace("\"driverId\":" + driver.getId(), "\"driverId\":null"),
                valid.replace("\"role\":\"PRIMARY\"", "\"role\":null"), valid.replace("PRIMARY", "UNKNOWN"))) {
            mvc.perform(post("/trip-drivers").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(0, tripDriverRepository.count());
    }

    @Test
    void missingReferencesAndResourcesReturnNotFound() throws Exception {
        for (String body : List.of(tripBody(999999L, vehicle.getId()), tripBody(route.getId(), 999999L))) {
            mvc.perform(post("/trips").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        Long tripId = idFromLocation(createTrip());
        for (String body : List.of(assignmentBody(999999L, driver.getId()), assignmentBody(tripId, 999999L))) {
            mvc.perform(post("/trip-drivers").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        for (String resource : List.of("trips", "trip-drivers")) {
            mvc.perform(get("/" + resource + "/999999")).andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
            mvc.perform(delete("/" + resource + "/999999")).andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(0, tripDriverRepository.count());
    }

    @Test
    void genericPatchAndAssignmentPutAreNotSupported() throws Exception {
        mvc.perform(put("/trip-drivers/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
        for (String resource : List.of("trips", "trip-drivers")) {
            mvc.perform(patch("/" + resource + "/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }

    private String createTrip() throws Exception {
        String location = mvc
                .perform(post("/trips").contentType(MediaType.APPLICATION_JSON)
                        .content(tripBody(route.getId(), vehicle.getId())))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.saleStatus").value("NOT_OPEN"))
                .andExpect(jsonPath("$.data.capacitySnapshot").value(45)).andReturn().getResponse()
                .getHeader("Location");
        assertNotNull(location);
        assertEquals("/trips/" + idFromLocation(location), location);
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

    @Test
    void findAllSupportsFiltersAndPagination() throws Exception {
        createTrip();

        mvc.perform(get("/trips").param("routeId", route.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(get("/trips").param("routeId", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get("/trips").param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(get("/trips").param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get("/trips").param("departureDate", "2026-10-23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mvc.perform(get("/trips").param("departureDate", "2026-10-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        mvc.perform(get("/trips").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Total-Count", "1"))
                .andExpect(header().string("X-Total-Pages", "1"))
                .andExpect(header().string("X-Page-Number", "0"))
                .andExpect(jsonPath("$.data.length()").value(1));
    }
}
