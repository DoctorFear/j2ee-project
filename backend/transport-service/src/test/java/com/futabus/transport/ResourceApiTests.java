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
        return Stream.of(
                new String[]{"vehicles", """
                        {"licensePlate":"51B-12345","type":"SEAT","capacity":45}
                        """},
                new String[]{"drivers", """
                        {"fullName":"Nguyen Van A","phone":"0901234567","licenseNumber":"GPLX123",
                         "licenseClass":"D","licenseExpiryOn":"2030-01-01T00:00:00"}
                        """}
        );
    }

    @ParameterizedTest
    @MethodSource("resources")
    void createReadAndDeleteUseStationResponseContract(String resource, String body) throws Exception {
        String path = "/api/" + resource;

        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());

        String location = mvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"))
                .andExpect(content().json("{\"data\":" + body + "}"))
                .andReturn().getResponse().getHeader("Location");

        mvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(content().json("{\"data\":" + body + "}"));

        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(content().json("{\"data\":[" + body + "]}"));

        mvc.perform(put(location).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(patch(location).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isMethodNotAllowed());

        mvc.perform(delete(location))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mvc.perform(get(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(delete(location))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
        mvc.perform(get(path))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
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
        mvc.perform(post("/api/" + resource)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        mvc.perform(get("/api/" + resource))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void driverLicenseFieldsAreOptional() throws Exception {
        mvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Nguyen Van B","phone":"0907654321"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }
}
