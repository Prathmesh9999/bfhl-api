package com.praxx.bfhl.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.praxx.bfhl.BfhlApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = BfhlApiApplication.class)
@AutoConfigureMockMvc
class BfhlControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postBfhlReturnsCategorizedResponseAndRequestId() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .header("X-Request-Id", "REQ-INT-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "data": ["A", "1", "22", "$", "B", "7"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.is_success").value(true))
                .andExpect(jsonPath("$.request_id").value("REQ-INT-1"))
                .andExpect(jsonPath("$.odd_numbers", hasSize(2)))
                .andExpect(jsonPath("$.even_numbers[0]").value("22"))
                .andExpect(jsonPath("$.sum").value("30"))
                .andExpect(jsonPath("$.processing_time_ms", greaterThanOrEqualTo(0)));
    }

    @Test
    void invalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/bfhl")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.is_success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void healthEndpointReturnsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("bfhl-api"));
    }
}
