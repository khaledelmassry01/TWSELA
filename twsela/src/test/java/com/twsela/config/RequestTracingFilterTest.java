package com.twsela.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RequestTracingFilterTest {

    private final RequestTracingFilter filter = new RequestTracingFilter();

    @Test
    @DisplayName("Generates X-Request-Id when not provided")
    void generatesRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            // Inside chain, MDC should have requestId
            assertNotNull(MDC.get("requestId"));
        });

        String requestId = response.getHeader("X-Request-Id");
        assertNotNull(requestId);
        assertEquals(8, requestId.length());
    }

    @Test
    @DisplayName("Uses incoming X-Request-Id header")
    void usesIncomingRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader("X-Request-Id", "abc12345");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals("abc12345", response.getHeader("X-Request-Id"));
    }

    @Test
    @DisplayName("MDC is cleaned after request")
    void mdcCleanedAfterRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertNull(MDC.get("requestId"), "MDC should be cleaned after filter");
    }

    @Test
    @DisplayName("Non-API requests don't log but still get traced")
    void nonApiRequestsStillTraced() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/index.html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertNotNull(response.getHeader("X-Request-Id"));
    }
}
