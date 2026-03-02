package com.twsela.web;

import com.twsela.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = HealthController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "app.version=1.0.1",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class HealthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean UserDetailsService userDetailsService;
    @MockBean JwtService jwtService;
    @MockBean com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean com.twsela.security.AuthenticationHelper authHelper;
    @MockBean DataSource dataSource;
    @MockBean StringRedisTemplate redisTemplate;

    @Test
    void health_returnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void health_returnsVersionAndTimestamp() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.version").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void health_returnsComponents() throws Exception {
        mockMvc.perform(get("/api/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.components").exists())
            .andExpect(jsonPath("$.components.database").exists())
            .andExpect(jsonPath("$.components.redis").exists());
    }
}
