package com.example.dto;

import com.example.model.User;

/**
 * DTO for login responses.
 * <p>
 * Maps to POST /api/auth/login response body.
 * Contains the session token and the authenticated user
 * (password excluded via {@code @JsonIgnore} on User.password).
 */
public class LoginResponse {

    private String token;

    private User user;

    public LoginResponse() {
    }

    public LoginResponse(String token, User user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
