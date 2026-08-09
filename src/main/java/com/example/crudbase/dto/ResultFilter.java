package com.example.crudbase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Optional filters applied to the results listing. Fields left unset are not filtered on.")
public class ResultFilter {

    @Schema(description = "Exact result identifier", example = "1")
    private Long id;

    @Schema(description = "Partial, case-insensitive match on the home team name", example = "Real")
    private String homeTeam;

    @Schema(description = "Partial, case-insensitive match on the away team name", example = "Barcelona")
    private String awayTeam;

    @Schema(description = "Exact number of goals scored by the home team", example = "2")
    private Integer homeScore;

    @Schema(description = "Exact number of goals scored by the away team", example = "1")
    private Integer awayScore;

    @Schema(description = "Exact match date", example = "2026-02-05")
    private LocalDate matchDate;

    @Schema(description = "Partial, case-insensitive match on the competition name", example = "La Liga")
    private String competition;

    @Schema(description = "Partial, case-insensitive match on the venue name", example = "Bernabeu")
    private String venue;
}
