package com.example.crudbase.repository;

import com.example.crudbase.dto.ResultFilter;
import com.example.crudbase.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ResultSpecificationsTest {

    @Autowired
    private ResultRepository resultRepository;

    private Result realMadridVsBarca;
    private Result racingVsIndependiente;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        realMadridVsBarca = resultRepository.save(Result.builder()
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .homeScore(2)
                .awayScore(1)
                .matchDate(LocalDate.of(2026, 2, 5))
                .competition("La Liga")
                .venue("Santiago Bernabeu")
                .build());
        racingVsIndependiente = resultRepository.save(Result.builder()
                .homeTeam("Racing")
                .awayTeam("Independiente")
                .homeScore(0)
                .awayScore(0)
                .matchDate(LocalDate.of(2026, 2, 6))
                .competition("Liga Profesional")
                .venue("El Cilindro")
                .build());
    }

    private List<Result> filter(ResultFilter filter) {
        Pageable pageable = PageRequest.of(0, 20);
        return resultRepository.findAll(ResultSpecifications.fromFilter(filter), pageable).getContent();
    }

    @Test
    void shouldReturnAllResultsWhenFilterIsEmpty() {
        List<Result> results = filter(ResultFilter.builder().build());

        assertThat(results).containsExactlyInAnyOrder(realMadridVsBarca, racingVsIndependiente);
    }

    @Test
    void shouldFilterByExactId() {
        List<Result> results = filter(ResultFilter.builder().id(realMadridVsBarca.getId()).build());

        assertThat(results).containsExactly(realMadridVsBarca);
    }

    @Test
    void shouldFilterByHomeTeamPartialCaseInsensitiveMatch() {
        List<Result> results = filter(ResultFilter.builder().homeTeam("real").build());

        assertThat(results).containsExactly(realMadridVsBarca);
    }

    @Test
    void shouldFilterByAwayTeamPartialCaseInsensitiveMatch() {
        List<Result> results = filter(ResultFilter.builder().awayTeam("indepen").build());

        assertThat(results).containsExactly(racingVsIndependiente);
    }

    @Test
    void shouldFilterByExactHomeScore() {
        List<Result> results = filter(ResultFilter.builder().homeScore(0).build());

        assertThat(results).containsExactly(racingVsIndependiente);
    }

    @Test
    void shouldFilterByExactAwayScore() {
        List<Result> results = filter(ResultFilter.builder().awayScore(1).build());

        assertThat(results).containsExactly(realMadridVsBarca);
    }

    @Test
    void shouldFilterByExactMatchDate() {
        List<Result> results = filter(ResultFilter.builder().matchDate(LocalDate.of(2026, 2, 6)).build());

        assertThat(results).containsExactly(racingVsIndependiente);
    }

    @Test
    void shouldFilterByCompetitionPartialCaseInsensitiveMatch() {
        List<Result> results = filter(ResultFilter.builder().competition("liga").build());

        assertThat(results).containsExactlyInAnyOrder(realMadridVsBarca, racingVsIndependiente);
    }

    @Test
    void shouldFilterByVenuePartialCaseInsensitiveMatch() {
        List<Result> results = filter(ResultFilter.builder().venue("cilindro").build());

        assertThat(results).containsExactly(racingVsIndependiente);
    }

    @Test
    void shouldCombineMultipleFiltersWithAnd() {
        List<Result> results = filter(ResultFilter.builder().homeTeam("Real").awayScore(1).build());

        assertThat(results).containsExactly(realMadridVsBarca);
    }

    @Test
    void shouldReturnNoResultsWhenCombinedFiltersDoNotMatchTheSameRecord() {
        List<Result> results = filter(ResultFilter.builder().homeTeam("Real").awayScore(0).build());

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnNoResultsWhenNoRecordMatchesTheFilter() {
        List<Result> results = filter(ResultFilter.builder().homeTeam("Nonexistent").build());

        assertThat(results).isEmpty();
    }
}
