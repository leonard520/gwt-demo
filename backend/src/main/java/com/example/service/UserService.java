package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.exception.LoginFailureException;
import com.example.model.User;
import com.example.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;

/**
 * Service handling user authentication and session management.
 * <p>
 * Rewritten from: com.example.server.service.UserServiceImpl
 * <p>
 * Business Logic:
 * <ul>
 *   <li>BL-001: User Login — validate, lookup, BCrypt verify, create session</li>
 *   <li>BL-002: User Logout — invalidate session</li>
 *   <li>BL-003: Session Recovery — return username from SecurityContext</li>
 * </ul>
 */
@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticate a user with username and password.
     * <p>
     * Source: UserServiceImpl.login(User user)
     * <p>
     * BL-001 Behavioral Spec:
     * <ul>
     *   <li>null/empty username or password → throw LoginFailureException (default message)</li>
     *   <li>user not found → throw LoginFailureException("Sorry, we couldn't locate you in our records.")</li>
     *   <li>wrong password → throw LoginFailureException (default message)</li>
     *   <li>success → create authenticated session, return LoginResponse(sessionId, user without password)</li>
     * </ul>
     *
     * @param loginRequest the login request containing username and password
     * @param session      the HTTP session
     * @return LoginResponse with session token and user info
     * @throws LoginFailureException if authentication fails
     */
    public LoginResponse login(LoginRequest loginRequest, HttpSession session) {
        try {
            // Validate input — matches source: user == null || user.isValid() == false
            if (loginRequest == null
                    || loginRequest.getUsername() == null || loginRequest.getUsername().isEmpty()
                    || loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                LOGGER.error("Bogus user data was received {}", loginRequest);
                throw new LoginFailureException();
            }

            // Lookup user by username
            User found = userRepository.findByUsername(loginRequest.getUsername())
                    .orElse(null);
            if (found == null) {
                throw new LoginFailureException("Sorry, we couldn't locate you in our records.");
            }

            // Validate password using BCrypt
            if (passwordEncoder.matches(loginRequest.getPassword(), found.getPassword())) {
                // Create authenticated session via SecurityContext
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                found.getUsername(), null, Collections.emptyList());
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authToken);
                session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

                String sessionId = session.getId();

                // Return user without password (password excluded via @JsonIgnore)
                User userResponse = new User(found.getUsername(), null);
                return new LoginResponse(sessionId, userResponse);
            } else {
                throw new LoginFailureException();
            }
        } catch (Exception e) {
            if (e instanceof LoginFailureException) {
                throw (LoginFailureException) e;
            }
            LOGGER.error("Unexpected error during login", e);
            throw new LoginFailureException();
        }
    }

    /**
     * Logout the current user by invalidating the session.
     * <p>
     * Source: UserServiceImpl.logout(String sessionId)
     * BL-002: Remove from sessionMap, invalidate HTTP session.
     *
     * @param session the HTTP session to invalidate
     */
    public void logout(HttpSession session) {
        SecurityContextHolder.clearContext();
        session.invalidate();
    }

    /**
     * Get the currently authenticated username.
     * <p>
     * Source: UserServiceImpl.isLoggedIn(String sessionId)
     * BL-003: Return the user associated with the current session.
     *
     * @param authentication the current authentication
     * @return the username of the authenticated user
     */
    public String getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        return authentication.getName();
    }
}
