package com.example.crudbase.cucumber.steps;

import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultStepDefinitions {

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private CommonStepDefinitions common;

    @Given("the results database is empty")
    public void theResultsDatabaseIsEmpty() {
        resultRepository.deleteAll();
        common.setLastCreatedId(null);
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
        common.setLastCreatedId(saved.getId());
    }

    @Then("the response should be a page of results")
    public void theResponseShouldBeAPageOfResults() {
        assertThat(common.getResponse().jsonPath().getList("content")).as("content").isNotNull();
    }

    @Then("the response should contain {int} result(s)")
    public void theResponseShouldContainNResults(int count) {
        assertThat(common.getResponse().jsonPath().getList("content")).hasSize(count);
    }

    @Then("the total number of results should be {int}")
    public void theTotalNumberOfResultsShouldBe(int total) {
        assertThat(common.getResponse().jsonPath().getLong("page.totalElements")).isEqualTo(total);
    }
}
