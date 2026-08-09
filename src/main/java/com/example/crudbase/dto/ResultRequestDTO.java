package com.example.crudbase.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;

public class ResultRequestDTO {

    @NotBlank(message = "homeTeam must not be blank")
    private String homeTeam;

    @NotBlank(message = "awayTeam must not be blank")
    private String awayTeam;

    @NotNull(message = "homeScore is required")
    @PositiveOrZero(message = "homeScore must not be negative")
    private Integer homeScore;

    @NotNull(message = "awayScore is required")
    @PositiveOrZero(message = "awayScore must not be negative")
    private Integer awayScore;

    @NotNull(message = "matchDate is required")
    private LocalDate matchDate;

    private String competition;
    private String venue;

    public ResultRequestDTO() {
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        this.homeTeam = homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public void setHomeScore(Integer homeScore) {
        this.homeScore = homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public void setAwayScore(Integer awayScore) {
        this.awayScore = awayScore;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDate matchDate) {
        this.matchDate = matchDate;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}
