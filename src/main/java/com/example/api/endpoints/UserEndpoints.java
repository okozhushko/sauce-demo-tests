package com.example.api.endpoints;

/**
 * Path constants for the user resource. Kept separate from {@code UserApiClient} so paths can be
 * reused (e.g. by future clients or reporting) without duplicating literals.
 */
public final class UserEndpoints {

    public static final String USERS = "/users";
    public static final String USER_BY_ID = "/users/{id}";

    private UserEndpoints() {
    }
}
