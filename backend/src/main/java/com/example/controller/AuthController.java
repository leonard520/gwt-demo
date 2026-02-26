package com.example.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.model.User;
import com.example.service.UserService;

import jakarta.servlet.http.HttpSession;

/**
 * REST controller for authentication endpoints.
 * <p>
 * Replaces the GWT-RPC UserService interface and RpcController front-controller
 * session validation pattern with Spring Security session-based REST endpoints.
 * <p>
 * Source: com.example.server.service.UserServiceImpl (login, logout, isLoggedIn)
 * Source: com.example.server.controller.RpcController (handleRequest, validateSession)
 * <p>
 * Business Logic:
 * <ul>
 *   <li>BL-001: User Login — POST /api/auth/login</li>
 *   <li>BL-002: User Logout — POST /api/auth/logout</li>
 *   <li>BL-003: Session Recovery — GET /api/auth/me</li>
 * </ul>
 * <p>
 * Requirements: REQ-001, REQ-002, REQ-003, REQ-021
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * POST /api/auth/login — Authenticate a user with username and password.
     * <p>
     * API Contract: accepts LoginRequest, returns LoginResponse (200) or ErrorResponse (401).
     * LoginFailureException is mapped to 401 by GlobalExceptionHandler.
     * <p>
     * Source: UserServiceImpl.login(User user) → String sessionId
     * BL-001: Validate → lookup → BCrypt verify → create session → return token + user
     *
     * @param loginRequest the login credentials
     * @param session      the HTTP session (created by container)
     * @return LoginResponse with session token and user info (password excluded)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
                                                HttpSession session) {
        LOGGER.debug("Login attempt for user: {}",
                loginRequest != null ? loginRequest.getUsername() : "null");
        LoginResponse response = userService.login(loginRequest, session);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout — Invalidate the current session.
     * <p>
     * API Contract: returns 204 No Content.
     * <p>
     * Source: UserServiceImpl.logout(String sessionId)
     * BL-002: Clear SecurityContext, invalidate HTTP session.
     *
     * @param session the HTTP session to invalidate
     * @return 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        LOGGER.debug("Logout request");
        userService.logout(session);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/auth/me — Get the currently authenticated user.
     * <p>
     * API Contract: returns User (200) with username field, or 401 if not authenticated.
     * Used for session recovery on page load.
     * <p>
     * Source: UserServiceImpl.isLoggedIn(String sessionId) → User
     * BL-003: Return user associated with current session (password never included).
     *
     * @param authentication the current Spring Security authentication
     * @return the authenticated user (username only, password excluded via @JsonIgnore)
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        String username = userService.getCurrentUser(authentication);
        if (username == null) {
            return ResponseEntity.status(401).build();
        }
        User user = new User(username, null);
        return ResponseEntity.ok(user);
    }
}
