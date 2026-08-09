package com.example.crudbase.cucumber.steps;

import com.example.crudbase.cucumber.support.AllureHttpAttachment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Holds the HTTP request/response steps shared by every entity's feature file: sending
 * a request, asserting on the status code, and asserting on response fields. Entity-specific
 * step classes ({@link ResultStepDefinitions}, {@link ClienteStepDefinitions}) inject this
 * class instead of redeclaring the same Cucumber expressions themselves — two step classes
 * each defining, say, {@code "I send a {word} request to {string}"} would make every
 * matching Gherkin step ambiguous, since Cucumber has no way to tell which one to run.
 * cucumber-spring instantiates one fresh instance of every glue class per scenario, so
 * autowiring this bean into the entity-specific classes shares state correctly within
 * a single scenario without leaking across scenarios.
 */
public class CommonStepDefinitions {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    private Response response;
    private Long lastCreatedId;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    public Response getResponse() {
        return response;
    }

    public void setLastCreatedId(Long lastCreatedId) {
        this.lastCreatedId = lastCreatedId;
    }

    @When("I send a {word} request to {string}")
    public void iSendARequestTo(String method, String path) {
        executeRequest(method, path, null);
    }

    @When("I send a {word} request to {string} with body:")
    public void iSendARequestToWithBody(String method, String path, String body) {
        executeRequest(method, path, body);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Then("the response field {string} should not be null")
    public void theResponseFieldShouldNotBeNull(String field) {
        Object value = response.jsonPath().get(field);
        assertThat(value).as("field '%s'", field).isNotNull();
    }

    @Then("the response field {string} should be {string}")
    public void theResponseFieldShouldBeString(String field, String expected) {
        assertThat(response.jsonPath().getString(field)).as("field '%s'", field).isEqualTo(expected);
    }

    @Then("the response field {string} should be {int}")
    public void theResponseFieldShouldBeInt(String field, int expected) {
        assertThat(response.jsonPath().getInt(field)).as("field '%s'", field).isEqualTo(expected);
    }

    @Then("the response should contain:")
    public void theResponseShouldContain(String expectedJson) throws Exception {
        JsonNode expected = OBJECT_MAPPER.readTree(expectedJson);
        JsonNode actual = OBJECT_MAPPER.readTree(response.getBody().asString());
        expected.properties().forEach(field -> {
            assertThat(actual.has(field.getKey()))
                    .as("Expected field '%s' to be present in the response", field.getKey())
                    .isTrue();
            assertThat(actual.get(field.getKey()))
                    .as("field '%s'", field.getKey())
                    .isEqualTo(field.getValue());
        });
    }

    private void executeRequest(String method, String path, String body) {
        String resolvedPath = path.replace("{lastId}", String.valueOf(lastCreatedId));
        String url = RestAssured.baseURI + ":" + RestAssured.port + resolvedPath;

        Map<String, String> requestHeaders = new LinkedHashMap<>();
        requestHeaders.put("Accept", ContentType.JSON.toString());
        if (body != null) {
            requestHeaders.put("Content-Type", ContentType.JSON.toString());
        }
        AllureHttpAttachment.attachRequest(method, url, requestHeaders, body);

        var requestSpec = RestAssured.given().accept(ContentType.JSON);
        if (body != null) {
            requestSpec = requestSpec.contentType(ContentType.JSON).body(body);
        }
        response = requestSpec.request(method, resolvedPath);

        AllureHttpAttachment.attachResponse(response);

        if ("POST".equalsIgnoreCase(method) && response.statusCode() == 201) {
            lastCreatedId = response.jsonPath().getLong("id");
        }
    }
}
