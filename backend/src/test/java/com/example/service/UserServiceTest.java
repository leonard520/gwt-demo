package com.example.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.dto.LoginRequest;
import com.example.dto.LoginResponse;
import com.example.exception.LoginFailureException;
import com.example.model.User;
import com.example.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

/**
 * Mockito-based unit tests for {@link UserService}.
 * <p>
 * Tests: login success, login user not found (custom message),
 * login wrong password, login null/empty fields, logout, getCurrentUser.
 * Verifies BCrypt usage.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession httpSession;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    // --- login success ---

    @Test
    void login_withValidCredentials_returnsLoginResponse() {
        LoginRequest request = new LoginRequest("davis", "davis");
        User storedUser = new User("davis", "$2a$10$hashedpassword");
        when(userRepository.findByUsername("davis")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("davis", "$2a$10$hashedpassword")).thenReturn(true);
        when(httpSession.getId()).thenReturn("session-123");

        LoginResponse response = userService.login(request, httpSession);

        assertNotNull(response);
        assertEquals("session-123", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("davis", response.getUser().getUsername());
        // Password should be null in response user (BL-011)
        assertNull(response.getUser().getPassword());

        verify(passwordEncoder).matches("davis", "$2a$10$hashedpassword");
        verify(userRepository).findByUsername("davis");
    }

    // --- login user not found ---

    @Test
    void login_whenUserNotFound_throwsLoginFailureExceptionWithCustomMessage() {
        LoginRequest request = new LoginRequest("unknown", "password");
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        LoginFailureException ex = assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        assertEquals("Sorry, we couldn't locate you in our records.", ex.getMessage());
        verify(userRepository).findByUsername("unknown");
        verifyNoInteractions(passwordEncoder);
    }

    // --- login wrong password ---

    @Test
    void login_whenWrongPassword_throwsLoginFailureException() {
        LoginRequest request = new LoginRequest("davis", "wrongpass");
        User storedUser = new User("davis", "$2a$10$hashedpassword");
        when(userRepository.findByUsername("davis")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("wrongpass", "$2a$10$hashedpassword")).thenReturn(false);

        LoginFailureException ex = assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        // Default message (not the custom "couldn't locate" message)
        assertNotEquals("Sorry, we couldn't locate you in our records.", ex.getMessage());
        verify(passwordEncoder).matches("wrongpass", "$2a$10$hashedpassword");
    }

    // --- login null/empty fields ---

    @Test
    void login_whenRequestIsNull_throwsLoginFailureException() {
        assertThrows(LoginFailureException.class,
                () -> userService.login(null, httpSession));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void login_whenUsernameIsNull_throwsLoginFailureException() {
        LoginRequest request = new LoginRequest(null, "password");

        assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        verifyNoInteractions(userRepository);
    }

    @Test
    void login_whenUsernameIsEmpty_throwsLoginFailureException() {
        LoginRequest request = new LoginRequest("", "password");

        assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        verifyNoInteractions(userRepository);
    }

    @Test
    void login_whenPasswordIsNull_throwsLoginFailureException() {
        LoginRequest request = new LoginRequest("davis", null);

        assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        verifyNoInteractions(userRepository);
    }

    @Test
    void login_whenPasswordIsEmpty_throwsLoginFailureException() {
        LoginRequest request = new LoginRequest("davis", "");

        assertThrows(LoginFailureException.class,
                () -> userService.login(request, httpSession));

        verifyNoInteractions(userRepository);
    }

    // --- logout ---

    @Test
    void logout_invalidatesSession() {
        userService.logout(httpSession);

        verify(httpSession).invalidate();
    }

    // --- getCurrentUser ---

    @Test
    void getCurrentUser_withValidAuthentication_returnsUsername() {
        Authentication auth = new UsernamePasswordAuthenticationToken("davis", null);

        String username = userService.getCurrentUser(auth);

        assertEquals("davis", username);
    }

    @Test
    void getCurrentUser_withNullAuthentication_returnsNull() {
        String username = userService.getCurrentUser(null);

        assertNull(username);
    }

    @Test
    void getCurrentUser_withNullPrincipal_returnsNull() {
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(null);

        String username = userService.getCurrentUser(auth);

        assertNull(username);
    }
}
