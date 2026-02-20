package com.nox.platform.shared.infra;

import com.nox.platform.shared.exception.DomainException;
import com.nox.platform.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    // A dummy controller to trigger the exceptions
    @RestController
    static class TestController {
        @GetMapping("/test/domain-exception")
        public void throwDomainException() {
            throw new DomainException("BUSINESS_ERROR", "A business rule failed", 400);
        }

        @GetMapping("/test/not-found-exception")
        public void throwNotFoundException() {
            throw new ResourceNotFoundException("User", "123");
        }

        @GetMapping("/test/unhandled-exception")
        public void throwUnhandledException() {
            throw new RuntimeException("Something completely unexpected happened");
        }
    }

    @Test
    void whenDomainExceptionThrown_thenReturns400AndApiError() throws Exception {
        mockMvc.perform(get("/test/domain-exception")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.error.message").value("A business rule failed"));
    }

    @Test
    void whenResourceNotFoundExceptionThrown_thenReturns404AndApiError() throws Exception {
        mockMvc.perform(get("/test/not-found-exception")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("User with identifier '123' was not found"));
    }

    @Test
    void whenUnhandledExceptionThrown_thenReturns500AndGenericApiError() throws Exception {
        mockMvc.perform(get("/test/unhandled-exception")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.message").value("An unexpected error occurred"));
    }
}
