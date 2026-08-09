package com.example.crudbase.mapper;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ResultMapperTest {

    private static final Long RESULT_ID = 1L;
    private static final LocalDate MATCH_DATE = LocalDate.of(2026, 2, 5);

    private ResultMapper resultMapper;

    @BeforeEach
    void setUp() {
        resultMapper = new ResultMapperImpl();
    }

    @Test
    void shouldMapRequestDtoFieldsToEntity() {
        ResultRequestDTO dto = ResultRequestDTO.builder()
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .homeScore(2)
                .awayScore(1)
                .matchDate(MATCH_DATE)
                .competition("La Liga")
                .venue("Santiago Bernabeu")
                .build();

        Result entity = resultMapper.toEntity(dto);

        assertThat(entity.getHomeTeam()).isEqualTo("Real Madrid");
        assertThat(entity.getAwayTeam()).isEqualTo("Barcelona");
        assertThat(entity.getHomeScore()).isEqualTo(2);
        assertThat(entity.getAwayScore()).isEqualTo(1);
        assertThat(entity.getMatchDate()).isEqualTo(MATCH_DATE);
        assertThat(entity.getCompetition()).isEqualTo("La Liga");
        assertThat(entity.getVenue()).isEqualTo("Santiago Bernabeu");
    }

    @Test
    void shouldIgnoreIdWhenMappingRequestDtoToNewEntity() {
        ResultRequestDTO dto = ResultRequestDTO.builder()
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .homeScore(2)
                .awayScore(1)
                .matchDate(MATCH_DATE)
                .build();

        Result entity = resultMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Result entity = Result.builder()
                .id(RESULT_ID)
                .homeTeam("Real Madrid")
                .awayTeam("Barcelona")
                .homeScore(2)
                .awayScore(1)
                .matchDate(MATCH_DATE)
                .competition("La Liga")
                .venue("Santiago Bernabeu")
                .build();

        ResultResponseDTO dto = resultMapper.toResponseDTO(entity);

        assertThat(dto.getId()).isEqualTo(RESULT_ID);
        assertThat(dto.getHomeTeam()).isEqualTo("Real Madrid");
        assertThat(dto.getAwayTeam()).isEqualTo("Barcelona");
        assertThat(dto.getHomeScore()).isEqualTo(2);
        assertThat(dto.getAwayScore()).isEqualTo(1);
        assertThat(dto.getMatchDate()).isEqualTo(MATCH_DATE);
        assertThat(dto.getCompetition()).isEqualTo("La Liga");
        assertThat(dto.getVenue()).isEqualTo("Santiago Bernabeu");
    }

    @Test
    void shouldUpdateAllMutableFieldsOnExistingEntity() {
        Result existing = Result.builder()
                .id(RESULT_ID)
                .homeTeam("Old Home")
                .awayTeam("Old Away")
                .homeScore(0)
                .awayScore(0)
                .matchDate(LocalDate.of(2020, 1, 1))
                .competition("Old Competition")
                .venue("Old Venue")
                .build();
        ResultRequestDTO dto = ResultRequestDTO.builder()
                .homeTeam("Updated Home")
                .awayTeam("Updated Away")
                .homeScore(9)
                .awayScore(8)
                .matchDate(MATCH_DATE)
                .competition("Updated Competition")
                .venue("Updated Venue")
                .build();

        resultMapper.updateEntity(dto, existing);

        assertThat(existing.getHomeTeam()).isEqualTo("Updated Home");
        assertThat(existing.getAwayTeam()).isEqualTo("Updated Away");
        assertThat(existing.getHomeScore()).isEqualTo(9);
        assertThat(existing.getAwayScore()).isEqualTo(8);
        assertThat(existing.getMatchDate()).isEqualTo(MATCH_DATE);
        assertThat(existing.getCompetition()).isEqualTo("Updated Competition");
        assertThat(existing.getVenue()).isEqualTo("Updated Venue");
    }

    @Test
    void shouldNotOverwriteIdWhenUpdatingEntity() {
        Result existing = Result.builder()
                .id(RESULT_ID)
                .homeTeam("Old Home")
                .awayTeam("Old Away")
                .homeScore(0)
                .awayScore(0)
                .matchDate(LocalDate.of(2020, 1, 1))
                .build();
        ResultRequestDTO dto = ResultRequestDTO.builder()
                .homeTeam("Updated Home")
                .awayTeam("Updated Away")
                .homeScore(9)
                .awayScore(8)
                .matchDate(MATCH_DATE)
                .build();

        resultMapper.updateEntity(dto, existing);

        assertThat(existing.getId()).isEqualTo(RESULT_ID);
    }
}
