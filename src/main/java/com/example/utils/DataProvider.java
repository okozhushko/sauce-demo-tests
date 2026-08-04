package com.example.utils;

import com.example.api.models.UserRequest;
import com.example.api.models.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Static TestNG {@code @DataProvider} suppliers backed by files under
 * {@code src/test/resources/testdata}. Reference a provider by name from a test class, e.g.:
 * <pre>
 *     {@literal @}Test(dataProvider = "users", dataProviderClass = com.example.utils.DataProvider.class)
 *     public void testGetUserById(UserResponse user) { ... }
 * </pre>
 *
 * <p>Available providers:
 * <ul>
 *     <li>{@code users} - one {@link UserResponse} per row, full record (incl. id) from users.json.</li>
 *     <li>{@code validUserRequests} - one {@link UserRequest} per row, built from users.json
 *         (id omitted) for create/update API test bodies.</li>
 *     <li>{@code userIds} - one {@code Integer} id per row from users.json, for get/delete-by-id tests.</li>
 * </ul>
 */
public final class DataProvider {

    private static final String USERS_JSON = "testdata/users.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DataProvider() {
    }

    @org.testng.annotations.DataProvider(name = "users")
    public static Object[][] users() {
        UserResponse[] users = loadUsers();
        Object[][] data = new Object[users.length][1];
        for (int i = 0; i < users.length; i++) {
            data[i][0] = users[i];
        }
        return data;
    }

    @org.testng.annotations.DataProvider(name = "validUserRequests")
    public static Object[][] validUserRequests() {
        UserResponse[] users = loadUsers();
        Object[][] data = new Object[users.length][1];
        for (int i = 0; i < users.length; i++) {
            UserResponse source = users[i];
            data[i][0] = new UserRequest()
                    .setName(source.getName())
                    .setEmail(source.getEmail())
                    .setUsername(source.getUsername())
                    .setRole(source.getRole());
        }
        return data;
    }

    @org.testng.annotations.DataProvider(name = "userIds")
    public static Object[][] userIds() {
        UserResponse[] users = loadUsers();
        Object[][] data = new Object[users.length][1];
        for (int i = 0; i < users.length; i++) {
            data[i][0] = users[i].getId();
        }
        return data;
    }

    private static UserResponse[] loadUsers() {
        try (InputStream input = DataProvider.class.getClassLoader().getResourceAsStream(USERS_JSON)) {
            if (input == null) {
                throw new IllegalStateException("Test data file not found on classpath: " + USERS_JSON);
            }
            return OBJECT_MAPPER.readValue(input, UserResponse[].class);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read test data file: " + USERS_JSON, e);
        }
    }
}
