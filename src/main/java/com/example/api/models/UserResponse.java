package com.example.api.models;

/**
 * Response DTO representing a user resource as returned by the API.
 * Requires a no-arg constructor and standard getters/setters so Jackson can
 * deserialize it via {@code response.as(UserResponse.class)}.
 */
public class UserResponse {

    private Integer id;
    private String name;
    private String email;
    private String username;
    private String role;

    public UserResponse() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserResponse{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", email='" + email + '\''
                + ", username='" + username + '\''
                + ", role='" + role + '\''
                + '}';
    }
}
