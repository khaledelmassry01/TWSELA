package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.SystemSettingRepository;
import com.twsela.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = SettingsController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
    }
)
class SettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean SystemSettingRepository settingRepository;
    @MockBean UserDetailsService userDetailsService;
    @MockBean JwtService jwtService;

    private Authentication createAuth(String role) {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        Role r = new Role(role);
        r.setId(1L);
        user.setRole(r);
        UserStatus status = new UserStatus("ACTIVE");
        status.setId(1L);
        user.setStatus(status);
        return new UsernamePasswordAuthenticationToken(user, null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role)));
    }

    @Test
    void getSettings_returnsDefaults() throws Exception {
        when(settingRepository.findByUserId(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/settings")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.language").value("ar"))
            .andExpect(jsonPath("$.currency").value("EGP"));
    }

    @Test
    void saveSettings_success() throws Exception {
        mockMvc.perform(post("/api/settings")
                .with(authentication(createAuth("MERCHANT")))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"language\":\"en\",\"darkMode\":\"true\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resetSettings_success() throws Exception {
        mockMvc.perform(post("/api/settings/reset")
                .with(authentication(createAuth("OWNER")))
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(settingRepository).deleteByUserId(1L);
    }
}
