package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ResultFilter;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @SuppressWarnings("unchecked")
    private Page<Result> stubRepositoryPage(Pageable pageable, List<Result> entities, long total) {
        Page<Result> page = new PageImpl<>(entities, pageable, total);
        when(resultRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        return page;
    }

    @Test
    void shouldReturnEmptyPageWhenNoResultsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        stubRepositoryPage(pageable, List.of(), 0);

        Page<ResultResponseDTO> results = resultService.findAll(ResultFilter.builder().build(), pageable);

        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
        verifyNoInteractions(resultMapper);
    }

    @Test
    void shouldReturnMappedPageWhenOneResultExists() {
        Pageable pageable = PageRequest.of(0, 20);
        Result entity = buildEntity();
        ResultResponseDTO dto = buildResponseDto();
        stubRepositoryPage(pageable, List.of(entity), 1);
        when(resultMapper.toResponseDTO(entity)).thenReturn(dto);

        Page<ResultResponseDTO> results = resultService.findAll(ResultFilter.builder().build(), pageable);

        assertThat(results.getContent()).containsExactly(dto);
        assertThat(results.getTotalElements()).isEqualTo(1);
        verify(resultMapper).toResponseDTO(entity);
    }

    @Test
    void shouldMapAllEntitiesInPageWhenMultipleResultsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Result entity1 = buildEntity();
        Result entity2 = buildEntity();
        entity2.setId(2L);
        ResultResponseDTO dto1 = buildResponseDto();
        ResultResponseDTO dto2 = new ResultResponseDTO();
        dto2.setId(2L);
        stubRepositoryPage(pageable, List.of(entity1, entity2), 2);
        when(resultMapper.toResponseDTO(entity1)).thenReturn(dto1);
        when(resultMapper.toResponseDTO(entity2)).thenReturn(dto2);

        Page<ResultResponseDTO> results = resultService.findAll(ResultFilter.builder().build(), pageable);

        assertThat(results.getContent()).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReportTotalPagesFromASecondPage() {
        Pageable pageable = PageRequest.of(1, 1);
        Result entity = buildEntity();
        stubRepositoryPage(pageable, List.of(entity), 2);
        when(resultMapper.toResponseDTO(entity)).thenReturn(buildResponseDto());

        Page<ResultResponseDTO> results = resultService.findAll(ResultFilter.builder().build(), pageable);

        assertThat(results.getNumber()).isEqualTo(1);
        assertThat(results.getTotalPages()).isEqualTo(2);
        assertThat(results.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldPassGivenFilterAndPageableToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        ResultFilter filter = ResultFilter.builder().homeTeam(HOME_TEAM).build();
        stubRepositoryPage(pageable, List.of(), 0);

        resultService.findAll(filter, pageable);

        verify(resultRepository).findAll(any(Specification.class), eq(pageable));
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

        verify(resultRepository, never()).delete(any(Result.class));
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
