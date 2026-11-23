package com.futabus.transport;

import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.trip.entity.Trip;
import com.futabus.transport.trip.entity.TripDriver;
import com.futabus.transport.trip.repository.TripDriverRepository;
import com.futabus.transport.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class ResourceApiTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    VehicleRepository vehicleRepository;

    @Autowired
    DriverRepository driverRepository;

    @Autowired
    TripRepository tripRepository;

    @Autowired
    TripDriverRepository tripDriverRepository;

    @Autowired
    RouteRepository routeRepository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        tripDriverRepository.deleteAll();
        tripRepository.deleteAll();
        routeRepository.deleteAll();
        vehicleRepository.deleteAll();
        driverRepository.deleteAll();
    }

    static Stream<String[]> resources() {
        return Stream.of(new String[] { "vehicles", """
                {"licensePlate":"51B-12345","type":"SEAT","capacity":45}
                """, "TRANSPORT_VEHICLE_NOT_FOUND" }, new String[] { "drivers", """
                {"fullName":"Nguyen Van A","phone":"0901234567","licenseNumber":"GPLX123",
                 "licenseClass":"D","licenseExpiryOn":"2030-01-01"}
                """, "TRANSPORT_DRIVER_NOT_FOUND" });
    }

    @ParameterizedTest
    @MethodSource("resources")
    void createReadAndDeleteUseStationResponseContract(String resource, String body, String notFoundCode)
            throws Exception {
        String path = "/" + resource;

        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());

        String location = mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"))
                .andExpect(content().json("{\"data\":" + body + "}")).andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(content().json("{\"data\":" + body + "}"));

        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1)).andExpect(content().json("{\"data\":[" + body + "]}"));

        if (resource.equals("drivers")) {
            mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                    .content(body.replace("}", ",\"status\":\"INACTIVE\"}"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));
        } else {
            mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                    .content(body.replace("}", ",\"status\":\"INACTIVE\"}"))).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("INACTIVE"));
            mvc.perform(put(location + "/status").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"MAINTENANCE\"}")).andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("MAINTENANCE"));
        }
        mvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));

        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value(notFoundCode));
        mvc.perform(delete(location)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "vehicles|{\"licensePlate\":\" \",\"type\":\"SEAT\",\"capacity\":45}",
            "vehicles|{\"licensePlate\":\"51B-12345\",\"capacity\":45}",
            "vehicles|{\"licensePlate\":\"51B-12345\",\"type\":\"UNKNOWN\",\"capacity\":45}",
            "vehicles|{\"licensePlate\":\"51B-12345\",\"type\":\"SEAT\"}",
            "vehicles|{\"licensePlate\":\"51B-12345\",\"type\":\"SEAT\",\"capacity\":0}",
            "drivers|{\"fullName\":\" \",\"phone\":\"0901234567\"}",
            "drivers|{\"fullName\":\"Nguyen Van A\"}",
            "drivers|{\"fullName\":\"Nguyen Van A\",\"phone\":\"0901234567\",\"licenseExpiryOn\":\"invalid\"}"
    }, delimiter = '|')
    void invalidCreationReturnsBadRequestWithoutSaving(String resource, String body) throws Exception {
        mvc.perform(post("/" + resource).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").exists());

        mvc.perform(get("/" + resource)).andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void driverLicenseFieldsAreOptional() throws Exception {
        mvc.perform(post("/drivers").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Nguyen Van B","phone":"0907654321"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void cannotDeleteVehicleWhenAssignedToTrip() throws Exception {
        Route route = routeRepository.save(Route.builder()
                .code("ROUTE-TEST-VEHICLE-DEL")
                .versionNo(1)
                .name("HCM - Vung Tau")
                .basePrice(new java.math.BigDecimal("150000"))
                .effectiveFromAt(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(com.futabus.transport.route.enums.RouteStatus.ACTIVE)
                .build());

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .licensePlate("51B-99999")
                .type(com.futabus.transport.resource.enums.ServiceType.SEAT)
                .capacity(45)
                .status(com.futabus.transport.resource.enums.VehicleStatus.ACTIVE)
                .build());

        tripRepository.save(Trip.builder()
                .code("TRIP-TEST-VEHICLE-DEL")
                .route(route)
                .vehicle(vehicle)
                .departureAt(java.time.LocalDateTime.of(2026, 10, 1, 8, 0))
                .arrivalAt(java.time.LocalDateTime.of(2026, 10, 1, 11, 0))
                .ticketPrice(new java.math.BigDecimal("150000"))
                .capacitySnapshot(45)
                .status(com.futabus.transport.trip.enums.TripStatus.DRAFT)
                .saleStatus(com.futabus.transport.trip.enums.SaleStatus.NOT_OPEN)
                .build());

        mvc.perform(delete("/vehicles/" + vehicle.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_VEHICLE_IN_USE"));
    }

    @Test
    void cannotDeleteDriverWhenAssignedToTrip() throws Exception {
        Route route = routeRepository.save(Route.builder()
                .code("ROUTE-TEST-DRIVER-DEL")
                .versionNo(1)
                .name("HCM - Phan Thiet")
                .basePrice(new java.math.BigDecimal("180000"))
                .effectiveFromAt(java.time.LocalDateTime.of(2026, 1, 1, 0, 0))
                .status(com.futabus.transport.route.enums.RouteStatus.ACTIVE)
                .build());

        Trip trip = tripRepository.save(Trip.builder()
                .code("TRIP-TEST-DRIVER-DEL")
                .route(route)
                .departureAt(java.time.LocalDateTime.of(2026, 10, 1, 8, 0))
                .arrivalAt(java.time.LocalDateTime.of(2026, 10, 1, 12, 0))
                .ticketPrice(new java.math.BigDecimal("180000"))
                .status(com.futabus.transport.trip.enums.TripStatus.DRAFT)
                .saleStatus(com.futabus.transport.trip.enums.SaleStatus.NOT_OPEN)
                .build());

        Driver driver = driverRepository.save(Driver.builder()
                .fullName("Nguyen Van Tai Xe")
                .phone("0988776655")
                .status(com.futabus.transport.resource.enums.DriverStatus.ACTIVE)
                .build());

        tripDriverRepository.save(TripDriver.builder()
                .trip(trip)
                .driver(driver)
                .role(com.futabus.transport.trip.enums.RoleDriver.PRIMARY)
                .build());

        mvc.perform(delete("/drivers/" + driver.getId()))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_DRIVER_IN_USE"));
    }
}
