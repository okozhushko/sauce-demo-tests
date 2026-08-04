package com.example.api.clients;

import com.example.api.endpoints.UserEndpoints;
import com.example.api.models.UserRequest;
import io.restassured.response.Response;

/**
 * Resource client for the user endpoints. Every method returns the raw {@link Response} -
 * status code checks and body assertions belong in the test, not here.
 */
public class UserApiClient extends ApiClient {

    public UserApiClient() {
        super();
    }

    public Response listUsers() {
        return get(UserEndpoints.USERS);
    }

    public Response getUserById(int id) {
        return get(UserEndpoints.USER_BY_ID, id);
    }

    public Response createUser(UserRequest userRequest) {
        return post(UserEndpoints.USERS, userRequest);
    }

    public Response updateUser(int id, UserRequest userRequest) {
        return put(UserEndpoints.USER_BY_ID, userRequest, id);
    }

    public Response deleteUser(int id) {
        return delete(UserEndpoints.USER_BY_ID, id);
    }
}
