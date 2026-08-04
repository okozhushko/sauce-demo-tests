package com.example.api.models;

/**
 * Request DTO for creating/updating a user resource.
 * Fluent setters allow inline construction, e.g.:
 * <pre>
 *     new UserRequest()
 *         .setName("John Doe")
 *         .setEmail("john.doe@example.com")
 *         .setUsername("jdoe")
 *         .setRole("user");
 * </pre>
 * Serialized to JSON by REST Assured (via Jackson) when passed to {@code .body(userRequest)}.
 */
public class UserRequest {

    private String name;
    private String email;
    private String username;
    private String role;

    public UserRequest() {
    }

    public String getName() {
        return name;
    }

    public UserRequest setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getRole() {
        return role;
    }

    public UserRequest setRole(String role) {
        this.role = role;
        return this;
    }

    @Override
    public String toString() {
        return "UserRequest{"
                + "name='" + name + '\''
                + ", email='" + email + '\''
                + ", username='" + username + '\''
                + ", role='" + role + '\''
                + '}';
    }
}
