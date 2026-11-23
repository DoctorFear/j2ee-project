package com.futabus.transport;

import com.futabus.transport.route.entity.Route;
import com.futabus.transport.route.entity.Station;
import com.futabus.transport.route.enums.RouteStatus;
import com.futabus.transport.route.enums.StationStatus;
import com.futabus.transport.route.enums.StationType;
import com.futabus.transport.route.repository.RouteRepository;
import com.futabus.transport.route.repository.RouteStopRepository;
import com.futabus.transport.route.repository.StationRepository;
import com.futabus.transport.route.repository.StationSegmentRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class RouteApiTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    RouteRepository routeRepository;

    @Autowired
    RouteStopRepository routeStopRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    StationSegmentRepository stationSegmentRepository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        cleanUp();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void cleanUp() {
        stationSegmentRepository.deleteAll();
        routeStopRepository.deleteAll();
        routeRepository.deleteAll();
        stationRepository.deleteAll();
    }

    @Test
    void stationSegmentUpdateKeepsSavedDurationWhenRequestIsInvalid() throws Exception {
        Station fromStation = station("Origin");
        Station toStation = station("Destination");
        String createBody = """
                {"fromStationId":%d,"toStationId":%d,"distanceKm":120.5,"durationMinutes":150}
                """.formatted(fromStation.getId(), toStation.getId());

        String location = mvc.perform(post("/stations/segments").contentType(MediaType.APPLICATION_JSON)
                .content(createBody)).andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");
        assertNotNull(location);

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content(createBody.replace("\"durationMinutes\":150", "\"durationMinutes\":180")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.durationMinutes").value(180));
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content(createBody.replace("\"durationMinutes\":150", "\"durationMinutes\":0")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.durationMinutes").exists());
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.data.durationMinutes").value(180));
    }

    @Test
    void routeCrudUsesResponseEnvelopeAndPersistsChanges() throws Exception {
        String body = routeBody("HCM-DL", 1);
        MvcResult created = mvc.perform(post("/routes").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.code").value("HCM-DL"))
                .andExpect(jsonPath("$.data.versionNo").value(1)).andExpect(jsonPath("$.data.basePrice").value(250000))
                .andExpect(jsonPath("$.data.status").value("ACTIVE")).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location);
        Long id = idFromLocation(location);
        assertEquals("/routes/" + id, location);

        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));
        mvc.perform(get("/routes")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON)
                .content(routeUpdateBody("HCM-DL", 1).replace("Ho Chi Minh - Da Lat", "Updated route")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Updated route"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        assertEquals("Updated route", routeRepository.findById(id).orElseThrow().getName());

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        assertFalse(routeRepository.existsById(id));
        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void omittedVersionAndStopPermissionsUseSchemaDefaults() throws Exception {
        String location = mvc
                .perform(post("/routes").contentType(MediaType.APPLICATION_JSON)
                        .content(routeBody("DEFAULTS", 1).replace("\"versionNo\":1,", "")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.versionNo").value(1)).andReturn()
                .getResponse().getHeader("Location");
        Station station = station("Terminal");
        mvc.perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(idFromLocation(location), station.getId(), 1)
                        .replace("\"allowPickup\":true,\"allowDropOff\":false", "\"allowDropOff\":false")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.allowPickup").value(false));
    }

    @Test
    void routeRejectsInvalidFieldsDatesAndEnumValues() throws Exception {
        String valid = routeBody("HCM-DL", 1);
        for (String invalid : List.of("{}", valid.replace("HCM-DL", " "),
                valid.replace("\"versionNo\":1", "\"versionNo\":0"), valid.replace("250000", "-1"),
                valid.replace("250000", "250000.50"), valid.replace("250000", "1000000000000000"),
                valid.replace("\"effectiveFromAt\":\"2026-10-21T08:00:00\"", "\"effectiveFromAt\":null"),
                valid.replace("\"effectiveToAt\":null", "\"effectiveToAt\":\"2026-10-20T08:00:00\""),
                valid.replace("\"effectiveToAt\":null", "\"effectiveToAt\":\"2026-10-21T08:00:00\""))) {
            mvc.perform(post("/routes").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.detail").exists());
        }
        Route route = route("HCM-DL", 1);
        for (String invalid : List.of(valid, routeUpdateBody("HCM-DL", 1).replace("ACTIVE", "UNKNOWN"))) {
            mvc.perform(put("/routes/{id}", route.getId()).contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        mvc.perform(put("/routes/{id}", route.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(routeUpdateBody("HCM-DL", 1).replace("\"effectiveToAt\":null",
                        "\"effectiveToAt\":\"2026-10-20T08:00:00\"")))
                .andExpect(status().isBadRequest());
        assertEquals(RouteStatus.INACTIVE, routeRepository.findById(route.getId()).orElseThrow().getStatus());
    }

    @Test
    void routeEnforcesUniqueCodeAndVersionOnCreateAndUpdate() throws Exception {
        Route first = route("HCM-DL", 1);
        first.setEffectiveToAt(LocalDateTime.of(2026, 10, 22, 8, 0));
        routeRepository.save(first);
        Route second = route("HCM-DL", 2);
        second.setEffectiveFromAt(first.getEffectiveToAt());
        second.setEffectiveToAt(LocalDateTime.of(2026, 10, 23, 8, 0));
        routeRepository.save(second);
        mvc.perform(post("/routes").contentType(MediaType.APPLICATION_JSON).content(routeBody("HCM-DL", 1)))
                .andExpect(status().isConflict()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(put("/routes/{id}", second.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(routeUpdateBody("HCM-DL", 1))).andExpect(status().isConflict());
        assertEquals(2, routeRepository.findById(second.getId()).orElseThrow().getVersionNo());
        mvc.perform(put("/routes/{id}", first.getId()).contentType(MediaType.APPLICATION_JSON)
                .content(routeUpdateBody("HCM-DL", 1).replace("\"effectiveToAt\":null",
                        "\"effectiveToAt\":\"2026-10-22T08:00:00\"")))
                .andExpect(status().isOk());
        mvc.perform(post("/routes").contentType(MediaType.APPLICATION_JSON).content(routeBody("HCM-DL", 3)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_ROUTE_PERIOD_OVERLAP"));
        mvc.perform(post("/routes").contentType(MediaType.APPLICATION_JSON)
                .content(routeBody("HCM-DL", 3).replace("2026-10-21T08:00:00", "2026-10-23T08:00:00")))
                .andExpect(status().isCreated());
    }

    @Test
    void routeMissingIdsReturnNotFound() throws Exception {
        mvc.perform(get("/routes/999999")).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(
                put("/routes/999999").contentType(MediaType.APPLICATION_JSON).content(routeUpdateBody("HCM-DL", 1)))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/routes/999999")).andExpect(status().isNotFound());
    }

    @Test
    void routeStopCrudCanChangeRouteStationAndStopDetails() throws Exception {
        Route firstRoute = route("HCM-DL", 1);
        Station firstStation = station("Mien Dong");
        MvcResult created = mvc
                .perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON)
                        .content(stopBody(firstRoute.getId(), firstStation.getId(), 1)))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.routeId").value(firstRoute.getId()))
                .andExpect(jsonPath("$.data.stationId").value(firstStation.getId()))
                .andExpect(jsonPath("$.data.allowDropOff").value(false))
                .andExpect(jsonPath("$.data.route").doesNotExist()).andReturn();
        String location = created.getResponse().getHeader("Location");
        assertNotNull(location);
        Long id = idFromLocation(location);
        assertEquals("/route-stops/" + id, location);
        mvc.perform(get(location)).andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));

        Route secondRoute = route("HCM-CT", 1);
        Station secondStation = station("Can Tho");
        String updated = stopBody(secondRoute.getId(), secondStation.getId(), 2)
                .replace("\"distanceKm\":0", "\"distanceKm\":150.25")
                .replace("\"offsetMinutes\":0", "\"offsetMinutes\":180")
                .replace("\"allowPickup\":true", "\"allowPickup\":false")
                .replace("\"allowDropOff\":false", "\"allowDropOff\":true");
        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(updated)).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routeId").value(secondRoute.getId()))
                .andExpect(jsonPath("$.data.stationId").value(secondStation.getId()))
                .andExpect(jsonPath("$.data.stopOrder").value(2)).andExpect(jsonPath("$.data.distanceKm").value(150.25))
                .andExpect(jsonPath("$.data.offsetMinutes").value(180))
                .andExpect(jsonPath("$.data.allowPickup").value(false))
                .andExpect(jsonPath("$.data.allowDropOff").value(true));
        var persisted = routeStopRepository.findById(id).orElseThrow();
        assertEquals(secondRoute.getId(), persisted.getRoute().getId());
        assertEquals(secondStation.getId(), persisted.getStation().getId());
        assertEquals(0, new BigDecimal("150.25").compareTo(persisted.getDistanceKm()));

        mvc.perform(delete(location)).andExpect(status().isNoContent()).andExpect(content().string(""));
        assertFalse(routeStopRepository.existsById(id));
        mvc.perform(get(location)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void routeStopListsAreOrderedAndCanBeFilteredByRoute() throws Exception {
        Route first = route("HCM-DL", 1);
        Route second = route("HCM-CT", 1);
        Station station = station("Mien Dong");
        createStop(first.getId(), station.getId(), 2);
        createStop(first.getId(), station.getId(), 1);
        createStop(second.getId(), station.getId(), 1);

        mvc.perform(get("/route-stops").param("routeId", first.getId().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].routeId").value(first.getId()))
                .andExpect(jsonPath("$.data[0].stopOrder").value(1))
                .andExpect(jsonPath("$.data[1].routeId").value(first.getId()))
                .andExpect(jsonPath("$.data[1].stopOrder").value(2));
        mvc.perform(get("/route-stops")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].stopOrder").value(1)).andExpect(jsonPath("$.data[1].stopOrder").value(2))
                .andExpect(jsonPath("$.data[2].routeId").value(second.getId()));
        Route empty = route("HCM-DN", 1);
        mvc.perform(get("/route-stops").param("routeId", empty.getId().toString())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        mvc.perform(get("/route-stops").param("routeId", "999999")).andExpect(status().isNotFound());
    }

    @Test
    void routeStopRejectsDuplicateOrderWithoutChangingExistingData() throws Exception {
        Route route = route("HCM-DL", 1);
        Station station = station("Mien Dong");
        createStop(route.getId(), station.getId(), 1);
        Long second = createStop(route.getId(), station.getId(), 2);
        mvc.perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(route.getId(), station.getId(), 1))).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(put("/route-stops/{id}", second).contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(route.getId(), station.getId(), 1))).andExpect(status().isConflict());
        assertEquals(2, routeStopRepository.findById(second).orElseThrow().getStopOrder());
        mvc.perform(put("/route-stops/{id}", second).contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(route.getId(), station.getId(), 2))).andExpect(status().isOk());
    }

    @Test
    void routeStopRejectsMissingReferencesOnCreateAndUpdate() throws Exception {
        Route route = route("HCM-DL", 1);
        Station station = station("Mien Dong");
        Long id = createStop(route.getId(), station.getId(), 1);
        for (String body : List.of(stopBody(999999L, station.getId(), 1), stopBody(route.getId(), 999999L, 1))) {
            mvc.perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
            mvc.perform(put("/route-stops/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(1, routeStopRepository.findById(id).orElseThrow().getStopOrder());
        assertEquals(1, routeStopRepository.count());
    }

    @Test
    void routeStopRejectsMissingAndInvalidFieldsOnCreateAndUpdate() throws Exception {
        Route route = route("HCM-DL", 1);
        Station station = station("Mien Dong");
        Long id = createStop(route.getId(), station.getId(), 1);
        String valid = stopBody(route.getId(), station.getId(), 1);
        for (String invalid : List.of("{}", valid.replace("\"routeId\":" + route.getId(), "\"routeId\":0"),
                valid.replace("\"stationId\":" + station.getId(), "\"stationId\":null"),
                valid.replace("\"stopOrder\":1", "\"stopOrder\":-1"),
                valid.replace("\"distanceKm\":0", "\"distanceKm\":-1"),
                valid.replace("\"distanceKm\":0", "\"distanceKm\":1.234"),
                valid.replace("\"distanceKm\":0", "\"distanceKm\":100000000"),
                valid.replace("\"offsetMinutes\":0", "\"offsetMinutes\":-1"),
                valid.replace("\"allowPickup\":true", "\"allowPickup\":null"),
                valid.replace("\"allowDropOff\":false", "\"allowDropOff\":null"))) {
            mvc.perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
            mvc.perform(put("/route-stops/{id}", id).contentType(MediaType.APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        }
        assertEquals(1, routeStopRepository.count());
        assertEquals(1, routeStopRepository.findById(id).orElseThrow().getStopOrder());
    }

    @Test
    void routeStopMissingIdsReturnNotFound() throws Exception {
        mvc.perform(get("/route-stops/999999")).andExpect(status().isNotFound());
        mvc.perform(put("/route-stops/999999").contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(999999L, 999999L, 1))).andExpect(status().isNotFound());
        mvc.perform(delete("/route-stops/999999")).andExpect(status().isNotFound());
    }

    @Test
    void referencedRouteAndStationCannotBeDeletedUntilStopsAreRemoved() throws Exception {
        Route route = route("HCM-DL", 1);
        Station station = station("Mien Dong");
        Long id = createStop(route.getId(), station.getId(), 1);
        mvc.perform(delete("/routes/{id}", route.getId())).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_ROUTE_HAS_STOPS"));
        mvc.perform(delete("/stations/{id}", station.getId())).andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("COMMON_DATA_CONFLICT"))
                .andExpect(jsonPath("$.detail").value("Request conflicts with existing data or references"));
        assertTrue(routeRepository.existsById(route.getId()));
        assertTrue(stationRepository.existsById(station.getId()));
        assertTrue(routeStopRepository.existsById(id));

        mvc.perform(delete("/route-stops/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(delete("/routes/{id}", route.getId())).andExpect(status().isNoContent());
        mvc.perform(delete("/stations/{id}", station.getId())).andExpect(status().isNoContent());
    }

    private Route route(String code, int version) {
        return routeRepository.save(Route.builder().code(code).versionNo(version).name("Ho Chi Minh - Da Lat")
                .basePrice(new BigDecimal("250000")).effectiveFromAt(LocalDateTime.of(2026, 10, 21, 8, 0))
                .status(RouteStatus.INACTIVE).build());
    }

    private Station station(String name) {
        return stationRepository
                .save(Station.builder().name(name).type(StationType.TERMINAL).status(StationStatus.INACTIVE).build());
    }

    private Long createStop(Long routeId, Long stationId, int order) throws Exception {
        MvcResult result = mvc.perform(post("/route-stops").contentType(MediaType.APPLICATION_JSON)
                .content(stopBody(routeId, stationId, order))).andExpect(status().isCreated()).andReturn();
        return idFromLocation(result.getResponse().getHeader("Location"));
    }

    private Long idFromLocation(String location) {
        assertNotNull(location);
        return Long.valueOf(location.substring(location.lastIndexOf('/') + 1));
    }

    private String routeBody(String code, int version) {
        return """
                {"code":"%s","versionNo":%d,"name":"Ho Chi Minh - Da Lat","basePrice":250000,
                "effectiveFromAt":"2026-10-21T08:00:00","effectiveToAt":null}
                """.formatted(code, version);
    }

    private String routeUpdateBody(String code, int version) {
        return routeBody(code, version).replace("\"effectiveToAt\":null",
                "\"effectiveToAt\":null,\"status\":\"ACTIVE\"");
    }

    private String stopBody(Long routeId, Long stationId, int order) {
        return """
                {"routeId":%d,"stationId":%d,"stopOrder":%d,"distanceKm":0,"offsetMinutes":0,
                "allowPickup":true,"allowDropOff":false}
                """.formatted(routeId, stationId, order);
    }
}
