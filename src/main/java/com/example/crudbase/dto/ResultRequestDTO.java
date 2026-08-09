package com.example.crudbase.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
@Schema(description = "Data required to create or update a match result")
public class ResultRequestDTO {

    @Schema(description = "Name of the home team", example = "Real Madrid", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "homeTeam must not be blank")
    private String homeTeam;

    @Schema(description = "Name of the away team", example = "Barcelona", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "awayTeam must not be blank")
    private String awayTeam;

    @Schema(description = "Goals scored by the home team", example = "2", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "homeScore is required")
    @PositiveOrZero(message = "homeScore must not be negative")
    private Integer homeScore;

    @Schema(description = "Goals scored by the away team", example = "1", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "awayScore is required")
    @PositiveOrZero(message = "awayScore must not be negative")
    private Integer awayScore;

    @Schema(description = "Date the match was played", example = "2026-02-05", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "matchDate is required")
    private LocalDate matchDate;

    @Schema(description = "Competition or tournament the match belongs to", example = "La Liga")
    private String competition;

    @Schema(description = "Venue where the match was played", example = "Santiago Bernabeu")
    private String venue;
}
