package com.example.crudbase.cucumber.steps;

import com.example.crudbase.cucumber.support.AllureHttpAttachment;
import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultStepDefinitions {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Autowired
    private ResultRepository resultRepository;

    private Response response;
    private Long lastResultId;

    @Before
    public void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Given("the results database is empty")
    public void theResultsDatabaseIsEmpty() {
        resultRepository.deleteAll();
        lastResultId = null;
    }

    @Given("a result exists with homeTeam {string}, awayTeam {string}, homeScore {int}, awayScore {int}, matchDate {string}")
    public void aResultExistsWith(String homeTeam, String awayTeam, int homeScore, int awayScore, String matchDate) {
        Result result = Result.builder()
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .matchDate(LocalDate.parse(matchDate))
                .build();
        Result saved = resultRepository.save(result);
        lastResultId = saved.getId();
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

    @Then("the response should be an array")
    public void theResponseShouldBeAnArray() {
        assertThat(response.getBody().asString().trim()).startsWith("[");
    }

    @Then("the response should contain {int} result(s)")
    public void theResponseShouldContainNResults(int count) {
        assertThat(response.jsonPath().getList("$")).hasSize(count);
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
        String resolvedPath = path.replace("{lastId}", String.valueOf(lastResultId));
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
            lastResultId = response.jsonPath().getLong("id");
        }
    }
}
