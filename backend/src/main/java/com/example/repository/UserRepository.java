package com.example.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.model.User;

/**
 * In-memory user repository.
 * <p>
 * Rewritten from: com.example.server.dao.UserDaoImpl
 * Uses HashMap&lt;String, User&gt; keyed by username (matching source).
 */
@Component
public class UserRepository {

    private final Map<String, User> map = new HashMap<>();

    /**
     * Find a user by username.
     * <p>
     * Source: UserDaoImpl.findByUserName(String) — returned null if not found.
     * Rewrite returns Optional for safer null handling.
     *
     * @param username the username to look up
     * @return an Optional containing the user, or empty if not found
     */
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(map.get(username));
    }

    /**
     * Save a user to the repository, keyed by username.
     *
     * @param user the user to save
     */
    public void save(User user) {
        if (user != null && user.getUsername() != null) {
            map.put(user.getUsername(), user);
        }
    }
}
