package com.twsela.web.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApiResponse — Factory Methods & Fields")
class ApiResponseTest {

    @Test
    @DisplayName("ok(data) — sets success=true, data, no message")
    void ok_data() {
        ApiResponse<Integer> r = ApiResponse.ok(42);
        assertTrue(r.isSuccess());
        assertEquals(42, r.getData());
        assertNull(r.getMessage());
        assertNull(r.getErrors());
        assertNotNull(r.getTimestamp());
    }

    @Test
    @DisplayName("ok(data, message) — sets both")
    void ok_dataMessage() {
        ApiResponse<Integer> r = ApiResponse.ok(42, "found");
        assertTrue(r.isSuccess());
        assertEquals(42, r.getData());
        assertEquals("found", r.getMessage());
    }

    @Test
    @DisplayName("ok(message) — no data")
    void ok_message() {
        ApiResponse<Void> r = ApiResponse.ok("done");
        assertTrue(r.isSuccess());
        assertEquals("done", r.getMessage());
        assertNull(r.getData());
    }

    @Test
    @DisplayName("error(message) — sets success=false")
    void error_message() {
        ApiResponse<Void> r = ApiResponse.error("bad");
        assertFalse(r.isSuccess());
        assertEquals("bad", r.getMessage());
        assertNull(r.getData());
        assertNull(r.getErrors());
    }

    @Test
    @DisplayName("error(message, errors) — includes error list")
    void error_withErrors() {
        List<String> errs = List.of("e1", "e2");
        ApiResponse<Void> r = ApiResponse.error("fail", errs);
        assertFalse(r.isSuccess());
        assertEquals("fail", r.getMessage());
        assertEquals(2, r.getErrors().size());
    }

    @Test
    @DisplayName("timestamp set on construction")
    void timestamp() {
        ApiResponse<Void> r = new ApiResponse<>();
        assertNotNull(r.getTimestamp());
    }
}
