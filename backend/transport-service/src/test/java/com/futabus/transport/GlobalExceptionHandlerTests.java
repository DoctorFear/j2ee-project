package com.futabus.transport;

import com.futabus.transport.common.handler.GlobalExceptionHandler;
import com.futabus.transport.trip.controller.DriverTripController;
import com.futabus.transport.trip.service.DriverTripService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTests {
    @Test
    void unexpectedDriverTripFailureReturnsASafeServerError() throws Exception {
        DriverTripService service = mock(DriverTripService.class);
        when(service.updateTripStatus("TRIP-1", "RUNNING"))
                .thenThrow(new IllegalStateException("Internal database details"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DriverTripController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(put("/driver/trips/TRIP-1/status").param("status", "RUNNING"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.errorCode").value("COMMON_INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }
}
