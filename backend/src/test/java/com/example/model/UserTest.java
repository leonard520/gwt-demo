package com.example.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link User}.
 * <p>
 * Rewritten from: com.example.client.model.UserTest
 * Uses JUnit 5 (Jupiter) instead of JUnit 4.
 * Tests cover: constructors, getters/setters, isValid() (BL-010),
 * equals, hashCode, toString masking (BL-011), and @JsonIgnore verification.
 */
class UserTest {

    private User user;
    private static final String NAME = "name";
    private static final String PASS = "pass";

    @BeforeEach
    void setUp() {
        user = new User();
    }

    // --- Constructor tests ---

    @Test
    void testDefaultConstructor() {
        assertNotNull(user);
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    void testTwoArgConstructor() {
        user = new User(NAME, PASS);
        assertEquals(NAME, user.getUsername());
        assertEquals(PASS, user.getPassword());
    }

    // --- Getter/Setter tests ---

    @Test
    void testGetUsername() {
        assertNull(user.getUsername());
    }

    @Test
    void testSetUsername() {
        user.setUsername(NAME);
        assertEquals(NAME, user.getUsername());
    }

    @Test
    void testGetPassword() {
        assertNull(user.getPassword());
    }

    @Test
    void testSetPassword() {
        user.setPassword(PASS);
        assertEquals(PASS, user.getPassword());
    }

    // --- isValid() tests (BL-010) ---

    @Test
    void testIsValidDefaultUser() {
        assertFalse(user.isValid(), "Default user with null fields should not be valid");
    }

    @Test
    void testIsValidUsernameOnly() {
        user.setUsername(NAME);
        assertFalse(user.isValid(), "User with only username should not be valid");
    }

    @Test
    void testIsValidPasswordOnly() {
        user.setPassword(PASS);
        assertFalse(user.isValid(), "User with only password should not be valid");
    }

    @Test
    void testIsValidBothSet() {
        user.setUsername(NAME);
        user.setPassword(PASS);
        assertTrue(user.isValid(), "User with both username and password should be valid");
    }

    @Test
    void testIsValidEmptyUsername() {
        user.setUsername("");
        user.setPassword(PASS);
        assertFalse(user.isValid(), "User with empty username should not be valid");
    }

    @Test
    void testIsValidEmptyPassword() {
        user.setUsername(NAME);
        user.setPassword("");
        assertFalse(user.isValid(), "User with empty password should not be valid");
    }

    @Test
    void testIsValidNullUsernameNonNullPassword() {
        user.setUsername(null);
        user.setPassword(PASS);
        assertFalse(user.isValid(), "User with null username should not be valid");
    }

    @Test
    void testIsValidFullCycleMatchingOriginalTest() {
        // Mirrors the exact sequence from the original UserTest.testIsValid()
        assertFalse(user.isValid());
        user.setUsername(NAME);
        assertFalse(user.isValid());
        user.setUsername(null);
        user.setPassword(PASS);
        assertFalse(user.isValid());
        user.setUsername(NAME);
        user.setPassword(PASS);
        assertTrue(user.isValid());
    }

    // --- equals() tests ---

    @Test
    void testEqualsIdentity() {
        assertEquals(user, user, "Same reference should be equal");
    }

    @Test
    void testEqualsNull() {
        assertNotEquals(null, user, "User should not equal null");
    }

    @Test
    void testEqualsDifferentClass() {
        assertNotEquals("not a user", user, "User should not equal a different class");
    }

    @Test
    void testEqualsSameValues() {
        User a = new User(NAME, PASS);
        User b = new User(NAME, PASS);
        assertEquals(a, b, "Users with same username and password should be equal");
    }

    @Test
    void testEqualsDifferentUsername() {
        User a = new User(NAME, PASS);
        User b = new User("other", PASS);
        assertNotEquals(a, b, "Users with different usernames should not be equal");
    }

    @Test
    void testEqualsDifferentPassword() {
        User a = new User(NAME, PASS);
        User b = new User(NAME, "other");
        assertNotEquals(a, b, "Users with different passwords should not be equal");
    }

    @Test
    void testEqualsNullFields() {
        User a = new User();
        User b = new User();
        assertEquals(a, b, "Two default users should be equal");
    }

    @Test
    void testEqualsOneNullUsername() {
        User a = new User();
        a.setPassword(PASS);
        User b = new User(NAME, PASS);
        assertNotEquals(a, b, "User with null username should not equal user with non-null username");
    }

    @Test
    void testEqualsOneNullPassword() {
        User a = new User();
        a.setUsername(NAME);
        User b = new User(NAME, PASS);
        assertNotEquals(a, b, "User with null password should not equal user with non-null password");
    }

    // --- hashCode() tests ---

    @Test
    void testHashCodeConsistency() {
        user = new User(NAME, PASS);
        int hash1 = user.hashCode();
        int hash2 = user.hashCode();
        assertEquals(hash1, hash2, "hashCode should be consistent across calls");
    }

    @Test
    void testHashCodeEqualObjects() {
        User a = new User(NAME, PASS);
        User b = new User(NAME, PASS);
        assertEquals(a.hashCode(), b.hashCode(), "Equal objects must have same hashCode");
    }

    @Test
    void testHashCodeDefaultUsers() {
        User a = new User();
        User b = new User();
        assertEquals(a.hashCode(), b.hashCode(), "Default users should have same hashCode");
    }

    // --- toString() tests (BL-011: password masking) ---

    @Test
    void testToStringMasksPassword() {
        user = new User(NAME, PASS);
        String result = user.toString();
        assertEquals("User [password=*******, username=" + NAME + "]", result);
    }

    @Test
    void testToStringDoesNotContainActualPassword() {
        user = new User(NAME, "secretPassword123");
        String result = user.toString();
        assertFalse(result.contains("secretPassword123"),
                "toString() must never expose the actual password");
        assertTrue(result.contains("*******"),
                "toString() must mask the password as *******");
    }

    @Test
    void testToStringContainsUsername() {
        user = new User(NAME, PASS);
        String result = user.toString();
        assertTrue(result.contains(NAME), "toString() must contain the username");
    }

    @Test
    void testToStringNullFields() {
        // Verify toString does not throw on default-constructed user
        String result = user.toString();
        assertNotNull(result);
        assertTrue(result.contains("*******"), "Password mask should always be present");
    }

    // --- @JsonIgnore verification ---

    @Test
    void testPasswordFieldHasJsonIgnoreAnnotation() throws NoSuchFieldException {
        var passwordField = User.class.getDeclaredField("password");
        JsonIgnore annotation = passwordField.getAnnotation(JsonIgnore.class);
        assertNotNull(annotation,
                "password field must have @JsonIgnore annotation to prevent exposure in API responses");
    }

    @Test
    void testUsernameFieldDoesNotHaveJsonIgnore() throws NoSuchFieldException {
        var usernameField = User.class.getDeclaredField("username");
        JsonIgnore annotation = usernameField.getAnnotation(JsonIgnore.class);
        assertNull(annotation,
                "username field should NOT have @JsonIgnore — it must be visible in API responses");
    }
}
