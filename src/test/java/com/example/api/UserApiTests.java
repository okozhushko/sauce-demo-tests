package com.example.api;

import com.example.api.clients.UserApiClient;
import com.example.api.models.UserRequest;
import com.example.api.models.UserResponse;
import com.example.base.BaseTest;
import com.example.retry.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

/**
 * CRUD coverage for the user resource, exercised against a public demo backend
 * (see {@code api.base.url} in application.properties/dev.properties) rather than a real
 * service. That backend (jsonplaceholder.typicode.com) fakes create/update/delete - it
 * returns a plausible response but doesn't actually persist the change - so these tests
 * assert on the echoed response shape rather than round-tripping with a follow-up GET.
 * Get/list/delete-by-id coverage uses real seed data (ids 1-10 only).
 *
 * <p>{@code retryAnalyzer} is attached to every test here (not just flaky-prone ones)
 * because every method makes a real outbound HTTP call to that third-party service -
 * exactly the kind of environment flakiness retries exist to absorb.
 */
@Story("User API")
public class UserApiTests extends BaseTest {

    private UserApiClient userApiClient;

    @BeforeClass
    public void setUpClient() {
        userApiClient = new UserApiClient();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,
            dataProvider = "userIds", dataProviderClass = com.example.utils.DataProvider.class)
    @Description("Get user by ID should return 200 and a user matching the requested id")
    public void testGetUserById(Integer id) {
        Response response = userApiClient.getUserById(id);

        Assert.assertEquals(response.getStatusCode(), 200);

        UserResponse user = response.as(UserResponse.class);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(user.getId(), id, "id should match the requested id");
        softAssert.assertNotNull(user.getName(), "name should be present");
        softAssert.assertNotNull(user.getEmail(), "email should be present");
        softAssert.assertNotNull(user.getUsername(), "username should be present");
        softAssert.assertAll();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Get user by a non-existent ID should return 404")
    public void testGetUserById_notFound() {
        Response response = userApiClient.getUserById(999999);

        Assert.assertEquals(response.getStatusCode(), 404);
    }

    @Test(retryAnalyzer = RetryAnalyzer.class)
    @Description("Listing users should return 200 and a non-empty collection")
    public void testListUsers() {
        Response response = userApiClient.listUsers();

        Assert.assertEquals(response.getStatusCode(), 200);

        UserResponse[] users = response.as(UserResponse[].class);
        Assert.assertTrue(users.length > 0, "expected at least one user in the list");
        Assert.assertNotNull(users[0].getId());
        Assert.assertNotNull(users[0].getName());
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,
            dataProvider = "validUserRequests", dataProviderClass = com.example.utils.DataProvider.class)
    @Description("Creating a user should return 201 with an id and echo the submitted fields")
    public void testCreateUser(UserRequest userRequest) {
        Response response = userApiClient.createUser(userRequest);

        Assert.assertEquals(response.getStatusCode(), 201);

        UserResponse created = response.as(UserResponse.class);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertNotNull(created.getId(), "created user should be assigned an id");
        softAssert.assertEquals(created.getName(), userRequest.getName());
        softAssert.assertEquals(created.getEmail(), userRequest.getEmail());
        softAssert.assertEquals(created.getUsername(), userRequest.getUsername());
        softAssert.assertAll();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,
            dataProvider = "users", dataProviderClass = com.example.utils.DataProvider.class)
    @Description("Updating an existing user should return 200 and reflect the new values")
    public void testUpdateUser(UserResponse existingUser) {
        UserRequest updateRequest = new UserRequest()
                .setName(existingUser.getName() + " Updated")
                .setEmail(existingUser.getEmail())
                .setUsername(existingUser.getUsername())
                .setRole(existingUser.getRole());

        Response response = userApiClient.updateUser(existingUser.getId(), updateRequest);

        Assert.assertEquals(response.getStatusCode(), 200);

        UserResponse updated = response.as(UserResponse.class);
        SoftAssert softAssert = new SoftAssert();
        softAssert.assertEquals(updated.getId(), existingUser.getId());
        softAssert.assertEquals(updated.getName(), updateRequest.getName());
        softAssert.assertEquals(updated.getEmail(), updateRequest.getEmail());
        softAssert.assertAll();
    }

    @Test(retryAnalyzer = RetryAnalyzer.class,
            dataProvider = "userIds", dataProviderClass = com.example.utils.DataProvider.class)
    @Description("Deleting a user should return 200")
    public void testDeleteUser(Integer id) {
        Response response = userApiClient.deleteUser(id);

        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
