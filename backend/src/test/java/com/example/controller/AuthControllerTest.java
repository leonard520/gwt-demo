package com.example.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.exception.LoginFailureException;
import com.example.model.User;
import com.example.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * Controller tests for {@link AuthController}.
 * <p>
 * Uses {@code @WebMvcTest} to test the web layer in isolation with mocked services.
 * Spring Security is active with default test configuration (HTTP Basic auth).
 * {@code @WithMockUser} simulates an authenticated user where required.
 * CSRF tokens are provided via {@code csrf()} for state-changing requests.
 * <p>
 * Source reference: com.example.server.controller.RpcControllerTest
 * (original tested session validation; rewrite tests REST endpoint behavior)
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Login success (200) with correct response body</li>
 *   <li>Login failure — user not found (401, custom message)</li>
 *   <li>Login failure — wrong password (401, default message)</li>
 *   <li>Logout (204)</li>
 *   <li>Get current user (200)</li>
 *   <li>Unauthenticated access (401)</li>
 * </ul>
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // ==================== Login Tests ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login — success returns 200 with token and user")
    void login_success_returns200WithTokenAndUser() throws Exception {
        LoginResponse response = new LoginResponse("session-abc-123", new User("davis", null));
        when(userService.login(any(LoginRequest.class), any(HttpSession.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"davis\",\"password\":\"davis\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("session-abc-123"))
                .andExpect(jsonPath("$.user.username").value("davis"))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        verify(userService).login(any(LoginRequest.class), any(HttpSession.class));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login — user not found returns 401 with custom message")
    void login_userNotFound_returns401WithCustomMessage() throws Exception {
        when(userService.login(any(LoginRequest.class), any(HttpSession.class)))
                .thenThrow(new LoginFailureException("Sorry, we couldn't locate you in our records."));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Sorry, we couldn't locate you in our records."))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/login — wrong password returns 401 with default message")
    void login_wrongPassword_returns401WithDefaultMessage() throws Exception {
        when(userService.login(any(LoginRequest.class), any(HttpSession.class)))
                .thenThrow(new LoginFailureException());

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"davis\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("The email or password you entered is incorrect."));
    }

    // ==================== Logout Tests ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/auth/logout — returns 204 No Content")
    void logout_returns204() throws Exception {
        doNothing().when(userService).logout(any(HttpSession.class));

        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).logout(any(HttpSession.class));
    }

    // ==================== Get Current User Tests ====================

    @Test
    @WithMockUser(username = "davis")
    @DisplayName("GET /api/auth/me — authenticated returns 200 with user")
    void getCurrentUser_authenticated_returns200WithUser() throws Exception {
        when(userService.getCurrentUser(any(Authentication.class)))
                .thenReturn("davis");

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("davis"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).getCurrentUser(any(Authentication.class));
    }

    // ==================== Unauthenticated Access Tests ====================

    @Test
    @DisplayName("GET /api/auth/me — unauthenticated returns 401")
    void getMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
