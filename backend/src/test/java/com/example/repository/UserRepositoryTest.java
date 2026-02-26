package com.example.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.model.User;

/**
 * Unit tests for {@link UserRepository}.
 * <p>
 * Tests: findByUsername (found/not found), save, multiple users.
 */
class UserRepositoryTest {

    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    void findByUsername_whenUserExists_returnsUser() {
        User user = new User("davis", "hashedpassword");
        userRepository.save(user);

        Optional<User> result = userRepository.findByUsername("davis");

        assertTrue(result.isPresent());
        assertEquals("davis", result.get().getUsername());
        assertEquals("hashedpassword", result.get().getPassword());
    }

    @Test
    void findByUsername_whenUserDoesNotExist_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_whenNullUsername_returnsEmpty() {
        Optional<User> result = userRepository.findByUsername(null);

        assertFalse(result.isPresent());
    }

    @Test
    void save_addsUserToRepository() {
        User user = new User("homer", "password123");
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("homer");
        assertTrue(found.isPresent());
        assertEquals("homer", found.get().getUsername());
    }

    @Test
    void save_nullUser_doesNotThrow() {
        assertDoesNotThrow(() -> userRepository.save(null));
    }

    @Test
    void save_userWithNullUsername_doesNotThrow() {
        User user = new User(null, "password");
        assertDoesNotThrow(() -> userRepository.save(user));
    }

    @Test
    void save_overwritesExistingUser() {
        userRepository.save(new User("davis", "oldpassword"));
        userRepository.save(new User("davis", "newpassword"));

        Optional<User> found = userRepository.findByUsername("davis");
        assertTrue(found.isPresent());
        assertEquals("newpassword", found.get().getPassword());
    }

    @Test
    void multipleUsers_savedAndRetrievedIndependently() {
        userRepository.save(new User("davis", "pass1"));
        userRepository.save(new User("homer", "pass2"));
        userRepository.save(new User("admin", "pass3"));

        assertTrue(userRepository.findByUsername("davis").isPresent());
        assertTrue(userRepository.findByUsername("homer").isPresent());
        assertTrue(userRepository.findByUsername("admin").isPresent());
        assertFalse(userRepository.findByUsername("unknown").isPresent());

        assertEquals("pass1", userRepository.findByUsername("davis").get().getPassword());
        assertEquals("pass2", userRepository.findByUsername("homer").get().getPassword());
        assertEquals("pass3", userRepository.findByUsername("admin").get().getPassword());
    }
}
