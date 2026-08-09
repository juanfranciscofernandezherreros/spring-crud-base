package com.example.crudbase.cucumber.steps;

import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
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
        Result result = new Result();
        result.setHomeTeam(homeTeam);
        result.setAwayTeam(awayTeam);
        result.setHomeScore(homeScore);
        result.setAwayScore(awayScore);
        result.setMatchDate(LocalDate.parse(matchDate));
        Result saved = resultRepository.save(result);
        lastResultId = saved.getId();
    }

    @When("I request all results")
    public void iRequestAllResults() {
        response = RestAssured.given()
                .accept(ContentType.JSON)
                .get("/api/results");
    }

    @When("I request the result by its id")
    public void iRequestTheResultByItsId() {
        response = RestAssured.given()
                .accept(ContentType.JSON)
                .get("/api/results/{id}", lastResultId);
    }

    @When("I request the result with id {long}")
    public void iRequestTheResultWithId(long id) {
        lastResultId = id;
        response = RestAssured.given()
                .accept(ContentType.JSON)
                .get("/api/results/{id}", id);
    }

    @When("I request the result with id {string}")
    public void iRequestTheResultWithInvalidId(String id) {
        response = RestAssured.given()
                .accept(ContentType.JSON)
                .get("/api/results/{id}", id);
    }

    @When("I create a result with homeTeam {string}, awayTeam {string}, homeScore {int}, awayScore {int}, matchDate {string}")
    public void iCreateAResultWith(String homeTeam, String awayTeam, int homeScore, int awayScore, String matchDate) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(buildRequestBody(homeTeam, awayTeam, homeScore, awayScore, matchDate))
                .post("/api/results");
        if (response.statusCode() == 201) {
            lastResultId = response.jsonPath().getLong("id");
        }
    }

    @When("I send a malformed JSON payload to create a result")
    public void iSendAMalformedJsonPayload() {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body("{ \"homeTeam\": \"River\", ")
                .post("/api/results");
    }

    @When("I update the result with homeTeam {string}, awayTeam {string}, homeScore {int}, awayScore {int}, matchDate {string}")
    public void iUpdateTheResultWith(String homeTeam, String awayTeam, int homeScore, int awayScore, String matchDate) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(buildRequestBody(homeTeam, awayTeam, homeScore, awayScore, matchDate))
                .put("/api/results/{id}", lastResultId);
    }

    @When("I update the result with id {long} using homeTeam {string}, awayTeam {string}, homeScore {int}, awayScore {int}, matchDate {string}")
    public void iUpdateTheResultWithId(long id, String homeTeam, String awayTeam, int homeScore, int awayScore, String matchDate) {
        response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(buildRequestBody(homeTeam, awayTeam, homeScore, awayScore, matchDate))
                .put("/api/results/{id}", id);
    }

    @When("I delete the result")
    public void iDeleteTheResult() {
        response = RestAssured.given()
                .delete("/api/results/{id}", lastResultId);
    }

    @When("I delete the result with id {long}")
    public void iDeleteTheResultWithId(long id) {
        response = RestAssured.given()
                .delete("/api/results/{id}", id);
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int status) {
        assertThat(response.statusCode()).isEqualTo(status);
    }

    @Then("the response content type should be JSON")
    public void theResponseContentTypeShouldBeJson() {
        assertThat(response.contentType()).contains(ContentType.JSON.toString());
    }

    @Then("the response should contain an empty array")
    public void theResponseShouldContainAnEmptyArray() {
        assertThat(response.jsonPath().getList("$")).isEmpty();
    }

    @Then("the response should contain {int} result(s)")
    public void theResponseShouldContainNResults(int count) {
        assertThat(response.jsonPath().getList("$")).hasSize(count);
    }

    @Then("the response should contain a result with homeTeam {string} and awayTeam {string}")
    public void theResponseShouldContainAResultWithHomeAndAwayTeam(String homeTeam, String awayTeam) {
        if (isArrayResponse()) {
            assertThat(response.jsonPath().getList("homeTeam", String.class)).contains(homeTeam);
            assertThat(response.jsonPath().getList("awayTeam", String.class)).contains(awayTeam);
        } else {
            assertThat(response.jsonPath().getString("homeTeam")).isEqualTo(homeTeam);
            assertThat(response.jsonPath().getString("awayTeam")).isEqualTo(awayTeam);
        }
    }

    @Then("the response should contain a result with homeScore {int} and awayScore {int}")
    public void theResponseShouldContainAResultWithScores(int homeScore, int awayScore) {
        assertThat(response.jsonPath().getInt("homeScore")).isEqualTo(homeScore);
        assertThat(response.jsonPath().getInt("awayScore")).isEqualTo(awayScore);
    }

    @Then("the response should contain a generated id")
    public void theResponseShouldContainAGeneratedId() {
        assertThat(response.jsonPath().getLong("id")).isPositive();
    }

    @Then("the error response should contain status {int}, error {string} and path {string}")
    public void theErrorResponseShouldContain(int status, String error, String path) {
        assertThat(response.jsonPath().getInt("status")).isEqualTo(status);
        assertThat(response.jsonPath().getString("error")).isEqualTo(error);
        assertThat(response.jsonPath().getString("path")).isEqualTo(path);
    }

    private boolean isArrayResponse() {
        return response.getBody().asString().trim().startsWith("[");
    }

    private Map<String, Object> buildRequestBody(String homeTeam, String awayTeam, int homeScore, int awayScore, String matchDate) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("homeTeam", homeTeam);
        body.put("awayTeam", awayTeam);
        body.put("homeScore", homeScore);
        body.put("awayScore", awayScore);
        body.put("matchDate", matchDate);
        return body;
    }
}
