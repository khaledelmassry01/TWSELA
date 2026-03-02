package com.twsela.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiVersionFilterTest {

    @Test
    @DisplayName("API_VERSION constant is set")
    void apiVersionConstant() {
        assertNotNull(ApiVersionFilter.API_VERSION);
        assertFalse(ApiVersionFilter.API_VERSION.isBlank());
    }

    @Test
    @DisplayName("API_VERSION_HEADER is X-API-Version")
    void apiVersionHeader() {
        assertEquals("X-API-Version", ApiVersionFilter.API_VERSION_HEADER);
    }

    @Test
    @DisplayName("Filter sets response headers")
    void filterSetsHeaders() throws Exception {
        ApiVersionFilter filter = new ApiVersionFilter();

        org.springframework.mock.web.MockHttpServletRequest request =
                new org.springframework.mock.web.MockHttpServletRequest();
        org.springframework.mock.web.MockHttpServletResponse response =
                new org.springframework.mock.web.MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {});

        assertEquals(ApiVersionFilter.API_VERSION, response.getHeader("X-API-Version"));
        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
    }
}
