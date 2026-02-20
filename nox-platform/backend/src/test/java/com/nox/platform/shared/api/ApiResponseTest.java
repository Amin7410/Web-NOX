package com.nox.platform.shared.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testOkResponse() {
        String data = "Test Data";
        ApiResponse<String> response = ApiResponse.ok(data);

        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getError());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponseWithoutDetails() {
        String code = "ERROR_CODE";
        String message = "Error Message";
        ApiResponse<Void> response = ApiResponse.error(code, message);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(code, response.getError().getCode());
        assertEquals(message, response.getError().getMessage());
        assertNull(response.getError().getDetails());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorResponseWithDetails() {
        String code = "ERROR_CODE";
        String message = "Error Message";
        String details = "Detailed context";
        ApiResponse<Void> response = ApiResponse.error(code, message, details);

        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNotNull(response.getError());
        assertEquals(code, response.getError().getCode());
        assertEquals(message, response.getError().getMessage());
        assertEquals(details, response.getError().getDetails());
        assertNotNull(response.getTimestamp());
    }
}
