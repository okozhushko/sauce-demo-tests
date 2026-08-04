package com.example.api.clients;

import com.example.config.ApiConfig;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thin base client wrapping REST Assured. Owns request building (base URI, content type,
 * timeouts, Allure request/response attachments) and exposes HTTP-verb helpers that return the
 * raw {@link Response}. It does not parse, assert, or interpret status codes - that is left to
 * the test or the calling resource client's caller.
 */
public class ApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);

    protected final String baseUri;
    protected final String basePath;

    protected ApiClient() {
        this(ApiConfig.getBaseUri(), "");
    }

    protected ApiClient(String baseUri, String basePath) {
        this.baseUri = baseUri;
        this.basePath = basePath;
    }

    private RestAssuredConfig restAssuredConfig() {
        int timeoutMs = ApiConfig.getConnectionTimeoutMs();
        return RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", timeoutMs)
                        .setParam("http.socket.timeout", timeoutMs));
    }

    protected RequestSpecification given() {
        return RestAssured
                .given()
                .config(restAssuredConfig())
                .baseUri(baseUri)
                .basePath(basePath)
                .contentType(ContentType.JSON)
                .filter(new AllureRestAssured())
                .log().all();
    }

    protected Response get(String endpoint, Object... pathParams) {
        logger.info("GET {}", endpoint);
        return given()
                .when()
                .get(endpoint, pathParams)
                .then()
                .log().all()
                .extract()
                .response();
    }

    protected Response post(String endpoint, Object body, Object... pathParams) {
        logger.info("POST {} with body: {}", endpoint, body);
        return given()
                .body(body)
                .when()
                .post(endpoint, pathParams)
                .then()
                .log().all()
                .extract()
                .response();
    }

    protected Response put(String endpoint, Object body, Object... pathParams) {
        logger.info("PUT {} with body: {}", endpoint, body);
        return given()
                .body(body)
                .when()
                .put(endpoint, pathParams)
                .then()
                .log().all()
                .extract()
                .response();
    }

    protected Response delete(String endpoint, Object... pathParams) {
        logger.info("DELETE {}", endpoint);
        return given()
                .when()
                .delete(endpoint, pathParams)
                .then()
                .log().all()
                .extract()
                .response();
    }
}
