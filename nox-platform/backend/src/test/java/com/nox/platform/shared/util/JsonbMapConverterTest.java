package com.nox.platform.shared.util;

import com.nox.platform.shared.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonbMapConverterTest {

    private JsonbMapConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JsonbMapConverter();
    }

    @Test
    void testConvertToDatabaseColumn_Success() {
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        map.put("number", 123);

        String result = converter.convertToDatabaseColumn(map);

        assertNotNull(result);
        assertTrue(result.contains("\"key\":\"value\""));
        assertTrue(result.contains("\"number\":123"));
    }

    @Test
    void testConvertToDatabaseColumn_Null() {
        String result = converter.convertToDatabaseColumn(null);
        assertEquals("{}", result);
    }

    @Test
    void testConvertToEntityAttribute_Success() {
        String json = "{\"key\":\"value\",\"number\":123}";

        Map<String, Object> result = converter.convertToEntityAttribute(json);

        assertNotNull(result);
        assertEquals("value", result.get("key"));
        assertEquals(123, result.get("number"));
    }

    @Test
    void testConvertToEntityAttribute_NullOrEmpty() {
        Map<String, Object> nullResult = converter.convertToEntityAttribute(null);
        assertNotNull(nullResult);
        assertTrue(nullResult.isEmpty());

        Map<String, Object> emptyResult = converter.convertToEntityAttribute("");
        assertNotNull(emptyResult);
        assertTrue(emptyResult.isEmpty());

        Map<String, Object> blankResult = converter.convertToEntityAttribute("   ");
        assertNotNull(blankResult);
        assertTrue(blankResult.isEmpty());
    }

    @Test
    void testConvertToEntityAttribute_MalformedJson() {
        String malformedJson = "{key:\"value\""; // Invalid JSON

        DomainException exception = assertThrows(DomainException.class, () -> {
            converter.convertToEntityAttribute(malformedJson);
        });

        assertEquals("JSON_DESERIALIZATION_ERROR", exception.getCode());
        assertEquals(500, exception.getStatus());
    }
}
