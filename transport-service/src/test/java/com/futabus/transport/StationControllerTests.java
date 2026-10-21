package com.futabus.transport;

import com.futabus.transport.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class StationControllerTests {
    @Autowired
    WebApplicationContext context;

    @Autowired
    StationRepository repository;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
        repository.deleteAll();
    }

    @Test
    void stationCrudUsesResponseEnvelope() throws Exception {
        mvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ben xe Mien Dong","address":"TP HCM","type":"TERMINAL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));

        Long id = repository.findAll().getFirst().getId();

        mvc.perform(get("/api/stations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Ben xe Mien Dong"));

        mvc.perform(put("/api/stations/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Van phong Mien Dong","address":"Binh Thanh","type":"OFFICE","status":"ACTIVE"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        mvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(id))
                .andExpect(jsonPath("$.data[0].type").value("OFFICE"));

        mvc.perform(delete("/api/stations/{id}", id))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/stations/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void invalidInputAndMissingIdReturnClientErrors() throws Exception {
        mvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":" ","address":"TP HCM","type":"TERMINAL"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());

        mvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ben xe","address":"TP HCM","type":"UNKNOWN"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        mvc.perform(put("/api/stations/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ben xe","address":"TP HCM","type":"TERMINAL","status":"ACTIVE"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));

        mvc.perform(delete("/api/stations/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
