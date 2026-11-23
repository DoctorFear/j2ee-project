package com.futabus.transport;

import com.futabus.transport.route.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class StationControllerTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    StationRepository stationRepository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        stationRepository.deleteAll();
    }

    @Test
    void stationCrudUsesResponseEnvelope() throws Exception {
        mvc.perform(post("/stations").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Ben xe Mien Dong","address":"TP HCM","type":"TERMINAL"}
                """)).andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.errorCode").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist()).andExpect(jsonPath("$.data.status").value("ACTIVE"));

        Long id = stationRepository.findAll().getFirst().getId();

        mvc.perform(get("/stations/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ben xe Mien Dong"));

        mvc.perform(put("/stations/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Van phong Mien Dong","address":"Binh Thanh","type":"OFFICE","status":"ACTIVE"}
                """)).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mvc.perform(get("/stations")).andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(id)).andExpect(jsonPath("$.data[0].type").value("OFFICE"));

        mvc.perform(delete("/stations/{id}", id)).andExpect(status().isNoContent());

        mvc.perform(get("/stations/{id}", id)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void invalidInputAndMissingIdReturnClientErrors() throws Exception {
        mvc.perform(post("/stations").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":" ","address":"TP HCM","type":"TERMINAL"}
                """)).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.detail").value(startsWith("name: ")))
                .andExpect(jsonPath("$.errors.name[0]").isString());

        mvc.perform(post("/stations").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Ben xe","address":"TP HCM","type":"UNKNOWN"}
                """)).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("COMMON_INVALID_JSON"));

        mvc.perform(put("/stations/999").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Ben xe","address":"TP HCM","type":"TERMINAL","status":"ACTIVE"}
                """)).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("TRANSPORT_STATION_NOT_FOUND"));

        mvc.perform(delete("/stations/999")).andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void validationReturnsErrorsForAllInvalidFields() throws Exception {
        mvc.perform(post("/stations").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request")).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.instance").value("/stations"))
                .andExpect(jsonPath("$.errorCode").value("COMMON_VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors.name[0]").isString())
                .andExpect(jsonPath("$.errors.address[0]").isString())
                .andExpect(jsonPath("$.errors.type[0]").isString())
                .andExpect(jsonPath("$.success").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.data").doesNotHaveJsonPath())
                .andExpect(jsonPath("$.message").doesNotHaveJsonPath());
    }

    @Test
    void springMvcErrorsUseTheSameEnvelopeAndPreserveProtocolHeaders() throws Exception {
        mvc.perform(get("/stations/invalid-id")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("COMMON_INVALID_PARAMETER"))
                .andExpect(jsonPath("$.errors.id[0]").isString());

        mvc.perform(get("/unknown-endpoint")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("COMMON_NOT_FOUND"));

        mvc.perform(patch("/stations/1")).andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("COMMON_METHOD_NOT_ALLOWED"));

        mvc.perform(post("/stations").contentType(MediaType.TEXT_PLAIN).content("invalid"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(header().exists("Accept"))
                .andExpect(jsonPath("$.errorCode").value("COMMON_UNSUPPORTED_MEDIA_TYPE"));

        mvc.perform(get("/stations").accept(MediaType.TEXT_PLAIN)).andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.errorCode").value("COMMON_NOT_ACCEPTABLE"));
    }
}
