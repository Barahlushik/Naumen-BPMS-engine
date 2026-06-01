package ru.naumen.bpms.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {
        SecurityConfig.class,
        SecurityConfigTest.TestMvcConfig.class
})
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("/api/admin/** должен перенаправлять неавторизованного пользователя на login")
    void adminApi_shouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(get("/api/admin/ping"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("/api/admin/** должен быть запрещен для ROLE_USER")
    void adminApi_shouldRejectUserRole() throws Exception {
        mockMvc.perform(get("/api/admin/ping").with(user("user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("/api/admin/** должен быть доступен для ROLE_ADMIN")
    void adminApi_shouldAllowAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/ping").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("обычные API должны быть доступны авторизованному ROLE_USER")
    void userApi_shouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/process-instances/ping").with(user("user").roles("USER")))
                .andExpect(status().isOk());
    }

    @Configuration
    @EnableWebMvc
    @Import(TestApiController.class)
    static class TestMvcConfig {
    }

    @RestController
    static class TestApiController {

        @GetMapping("/api/admin/ping")
        ResponseEntity<String> adminPing() {
            return ResponseEntity.ok("admin");
        }

        @GetMapping("/api/process-instances/ping")
        ResponseEntity<String> userPing() {
            return ResponseEntity.ok("user");
        }
    }
}
