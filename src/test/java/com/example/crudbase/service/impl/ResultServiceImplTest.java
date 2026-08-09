package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.exception.ResourceNotFoundException;
import com.example.crudbase.mapper.ResultMapper;
import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResultServiceImplTest {

    private static final Long RESULT_ID = 1L;
    private static final Long UNKNOWN_ID = 999L;
    private static final String HOME_TEAM = "Real Madrid";
    private static final String AWAY_TEAM = "Barcelona";
    private static final Integer HOME_SCORE = 2;
    private static final Integer AWAY_SCORE = 1;
    private static final LocalDate MATCH_DATE = LocalDate.of(2026, 2, 5);

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private ResultMapper resultMapper;

    private SimpleMeterRegistry meterRegistry;

    private ResultServiceImpl resultService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        resultService = new ResultServiceImpl(resultRepository, resultMapper, meterRegistry);
    }

    // ---- findAll ----

    @Test
    void shouldReturnEmptyListWhenNoResultsExist() {
        when(resultRepository.findAll()).thenReturn(List.of());

        List<ResultResponseDTO> results = resultService.findAll();

        assertThat(results).isEmpty();
        verify(resultRepository).findAll();
        verifyNoInteractions(resultMapper);
    }

    @Test
    void shouldReturnSingleResultWhenOneResultExists() {
        Result entity = buildEntity();
        ResultResponseDTO dto = buildResponseDto();
        when(resultRepository.findAll()).thenReturn(List.of(entity));
        when(resultMapper.toResponseDTO(entity)).thenReturn(dto);

        List<ResultResponseDTO> results = resultService.findAll();

        assertThat(results).containsExactly(dto);
        verify(resultMapper).toResponseDTO(entity);
    }

    @Test
    void shouldMapAllEntitiesWhenMultipleResultsExist() {
        Result entity1 = buildEntity();
        Result entity2 = buildEntity();
        entity2.setId(2L);
        ResultResponseDTO dto1 = buildResponseDto();
        ResultResponseDTO dto2 = new ResultResponseDTO();
        dto2.setId(2L);
        when(resultRepository.findAll()).thenReturn(List.of(entity1, entity2));
        when(resultMapper.toResponseDTO(entity1)).thenReturn(dto1);
        when(resultMapper.toResponseDTO(entity2)).thenReturn(dto2);

        List<ResultResponseDTO> results = resultService.findAll();

        assertThat(results).containsExactly(dto1, dto2);
        verify(resultMapper).toResponseDTO(entity1);
        verify(resultMapper).toResponseDTO(entity2);
    }

    @Test
    void shouldCallRepositoryExactlyOnceWhenFindingAllResults() {
        when(resultRepository.findAll()).thenReturn(List.of());

        resultService.findAll();

        verify(resultRepository, times(1)).findAll();
        verifyNoMoreInteractions(resultRepository);
    }

    // ---- findById ----

    @Test
    void shouldReturnResultWhenIdExists() {
        Result entity = buildEntity();
        ResultResponseDTO dto = buildResponseDto();
        when(resultRepository.findById(RESULT_ID)).thenReturn(Optional.of(entity));
        when(resultMapper.toResponseDTO(entity)).thenReturn(dto);

        ResultResponseDTO result = resultService.findById(RESULT_ID);

        assertThat(result).isEqualTo(dto);
        verify(resultRepository).findById(RESULT_ID);
        verify(resultMapper).toResponseDTO(entity);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingUnknownId() {
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.findById(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotCallMapperWhenFindingUnknownId() {
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.findById(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(resultMapper);
    }

    // ---- create ----

    @Test
    void shouldCreateResult() {
        ResultRequestDTO requestDto = buildRequestDto();
        Result entityToSave = buildEntity();
        entityToSave.setId(null);
        Result savedEntity = buildEntity();
        ResultResponseDTO responseDto = buildResponseDto();
        when(resultMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(resultRepository.save(entityToSave)).thenReturn(savedEntity);
        when(resultMapper.toResponseDTO(savedEntity)).thenReturn(responseDto);

        ResultResponseDTO result = resultService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(meterRegistry.counter("results.created").count()).isEqualTo(1.0);
        verify(resultMapper).toEntity(requestDto);
        verify(resultRepository).save(entityToSave);
        verify(resultMapper).toResponseDTO(savedEntity);
    }

    @Test
    void shouldReturnGeneratedIdAfterCreatingResult() {
        ResultRequestDTO requestDto = buildRequestDto();
        Result entityToSave = buildEntity();
        entityToSave.setId(null);
        Result savedEntity = buildEntity();
        ResultResponseDTO responseDto = buildResponseDto();
        when(resultMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(resultRepository.save(entityToSave)).thenReturn(savedEntity);
        when(resultMapper.toResponseDTO(savedEntity)).thenReturn(responseDto);

        ResultResponseDTO result = resultService.create(requestDto);

        assertThat(result.getId()).isEqualTo(RESULT_ID);
    }

    @Test
    void shouldPersistEntityMappedFromRequestWhenCreatingResult() {
        ResultRequestDTO requestDto = buildRequestDto();
        Result entityToSave = buildEntity();
        when(resultMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(resultRepository.save(entityToSave)).thenReturn(entityToSave);
        when(resultMapper.toResponseDTO(entityToSave)).thenReturn(buildResponseDto());

        resultService.create(requestDto);

        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(resultRepository).save(captor.capture());
        assertThat(captor.getValue().getHomeTeam()).isEqualTo(HOME_TEAM);
        assertThat(captor.getValue().getAwayTeam()).isEqualTo(AWAY_TEAM);
        assertThat(captor.getValue().getHomeScore()).isEqualTo(HOME_SCORE);
        assertThat(captor.getValue().getAwayScore()).isEqualTo(AWAY_SCORE);
    }

    // ---- update ----

    @Test
    void shouldUpdateExistingResult() {
        Result existing = buildEntity();
        ResultRequestDTO requestDto = buildRequestDtoWithUpdatedScore();
        ResultResponseDTO responseDto = buildResponseDto();
        when(resultRepository.findById(RESULT_ID)).thenReturn(Optional.of(existing));
        when(resultRepository.save(existing)).thenReturn(existing);
        when(resultMapper.toResponseDTO(existing)).thenReturn(responseDto);

        ResultResponseDTO result = resultService.update(RESULT_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(resultMapper).updateEntity(requestDto, existing);
        verify(resultRepository).save(existing);
    }

    @Test
    void shouldDelegateFieldCopyingToMapperBeforeSaving() {
        Result existing = buildEntity();
        ResultRequestDTO requestDto = buildRequestDto();
        when(resultRepository.findById(RESULT_ID)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            Result target = invocation.getArgument(1);
            target.setHomeTeam("Updated Home");
            return null;
        }).when(resultMapper).updateEntity(requestDto, existing);
        when(resultRepository.save(existing)).thenReturn(existing);
        when(resultMapper.toResponseDTO(existing)).thenReturn(buildResponseDto());

        resultService.update(RESULT_ID, requestDto);

        ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(resultRepository).save(captor.capture());
        assertThat(captor.getValue().getHomeTeam()).isEqualTo("Updated Home");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingUnknownResult() {
        ResultRequestDTO requestDto = buildRequestDto();
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.update(UNKNOWN_ID, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotSaveWhenUpdatingUnknownResult() {
        ResultRequestDTO requestDto = buildRequestDto();
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.update(UNKNOWN_ID, requestDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resultMapper, never()).updateEntity(any(), any());
        verify(resultRepository, never()).save(any());
    }

    // ---- delete ----

    @Test
    void shouldDeleteExistingResult() {
        Result existing = buildEntity();
        when(resultRepository.findById(RESULT_ID)).thenReturn(Optional.of(existing));

        resultService.delete(RESULT_ID);

        verify(resultRepository).delete(existing);
        assertThat(meterRegistry.counter("results.deleted").count()).isEqualTo(1.0);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingUnknownResult() {
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.delete(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotDeleteWhenResultDoesNotExist() {
        when(resultRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resultService.delete(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(resultRepository, never()).delete(any());
    }

    // ---- test data builders ----

    private Result buildEntity() {
        Result entity = new Result();
        entity.setId(RESULT_ID);
        entity.setHomeTeam(HOME_TEAM);
        entity.setAwayTeam(AWAY_TEAM);
        entity.setHomeScore(HOME_SCORE);
        entity.setAwayScore(AWAY_SCORE);
        entity.setMatchDate(MATCH_DATE);
        return entity;
    }

    private ResultRequestDTO buildRequestDto() {
        ResultRequestDTO dto = new ResultRequestDTO();
        dto.setHomeTeam(HOME_TEAM);
        dto.setAwayTeam(AWAY_TEAM);
        dto.setHomeScore(HOME_SCORE);
        dto.setAwayScore(AWAY_SCORE);
        dto.setMatchDate(MATCH_DATE);
        return dto;
    }

    private ResultRequestDTO buildRequestDtoWithUpdatedScore() {
        ResultRequestDTO dto = buildRequestDto();
        dto.setHomeScore(5);
        dto.setAwayScore(3);
        return dto;
    }

    private ResultResponseDTO buildResponseDto() {
        ResultResponseDTO dto = new ResultResponseDTO();
        dto.setId(RESULT_ID);
        dto.setHomeTeam(HOME_TEAM);
        dto.setAwayTeam(AWAY_TEAM);
        dto.setHomeScore(HOME_SCORE);
        dto.setAwayScore(AWAY_SCORE);
        dto.setMatchDate(MATCH_DATE);
        return dto;
    }
}
