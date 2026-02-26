package com.example.dto;

/**
 * DTO for login requests.
 * <p>
 * Maps to POST /api/auth/login request body.
 * Source: com.example.client.service.UserService.login(User user)
 */
public class LoginRequest {

    private String username;

    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
