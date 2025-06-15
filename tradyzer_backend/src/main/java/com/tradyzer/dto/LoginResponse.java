package com.tradyzer.dto;

import com.tradyzer.entity.UserRole;
import java.util.Set;

public class LoginResponse {
    private String token;
    private String username;
    private String email;
    private String fullName;
    private Set<UserRole> roles;

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }
}