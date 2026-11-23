package com.futabus.transport;

import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.resource.repository.VehicleRepository;
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

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
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
}
