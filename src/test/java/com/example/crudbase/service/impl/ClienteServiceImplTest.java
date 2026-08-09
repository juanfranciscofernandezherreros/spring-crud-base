package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import com.example.crudbase.exception.ResourceNotFoundException;
import com.example.crudbase.mapper.ClienteMapper;
import com.example.crudbase.model.Cliente;
import com.example.crudbase.repository.ClienteRepository;
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

import java.time.Instant;
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
class ClienteServiceImplTest {

    private static final Long CLIENTE_ID = 1L;
    private static final Long UNKNOWN_ID = 999L;
    private static final String FIRST_NAME = "Juan";
    private static final String LAST_NAME = "Fernandez";
    private static final String EMAIL = "juan.fernandez@example.com";
    private static final String PHONE = "+34600123456";
    private static final String ADDRESS = "Calle Mayor 1, Madrid";
    private static final Instant CREATED_AT = Instant.parse("2026-02-05T10:15:30Z");

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ClienteMapper clienteMapper;

    private SimpleMeterRegistry meterRegistry;

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        clienteService = new ClienteServiceImpl(clienteRepository, clienteMapper, meterRegistry);
    }

    // ---- findAll ----

    @SuppressWarnings("unchecked")
    private Page<Cliente> stubRepositoryPage(Pageable pageable, List<Cliente> entities, long total) {
        Page<Cliente> page = new PageImpl<>(entities, pageable, total);
        when(clienteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        return page;
    }

    @Test
    void shouldReturnEmptyPageWhenNoClientsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        stubRepositoryPage(pageable, List.of(), 0);

        Page<ClienteResponseDTO> results = clienteService.findAll(ClienteFilter.builder().build(), pageable);

        assertThat(results.getContent()).isEmpty();
        assertThat(results.getTotalElements()).isZero();
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void shouldReturnMappedPageWhenOneClientExists() {
        Pageable pageable = PageRequest.of(0, 20);
        Cliente entity = buildEntity();
        ClienteResponseDTO dto = buildResponseDto();
        stubRepositoryPage(pageable, List.of(entity), 1);
        when(clienteMapper.toResponseDTO(entity)).thenReturn(dto);

        Page<ClienteResponseDTO> results = clienteService.findAll(ClienteFilter.builder().build(), pageable);

        assertThat(results.getContent()).containsExactly(dto);
        assertThat(results.getTotalElements()).isEqualTo(1);
        verify(clienteMapper).toResponseDTO(entity);
    }

    @Test
    void shouldMapAllEntitiesInPageWhenMultipleClientsExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Cliente entity1 = buildEntity();
        Cliente entity2 = buildEntity();
        entity2.setId(2L);
        ClienteResponseDTO dto1 = buildResponseDto();
        ClienteResponseDTO dto2 = new ClienteResponseDTO();
        dto2.setId(2L);
        stubRepositoryPage(pageable, List.of(entity1, entity2), 2);
        when(clienteMapper.toResponseDTO(entity1)).thenReturn(dto1);
        when(clienteMapper.toResponseDTO(entity2)).thenReturn(dto2);

        Page<ClienteResponseDTO> results = clienteService.findAll(ClienteFilter.builder().build(), pageable);

        assertThat(results.getContent()).containsExactly(dto1, dto2);
    }

    @Test
    void shouldReportTotalPagesFromASecondPage() {
        Pageable pageable = PageRequest.of(1, 1);
        Cliente entity = buildEntity();
        stubRepositoryPage(pageable, List.of(entity), 2);
        when(clienteMapper.toResponseDTO(entity)).thenReturn(buildResponseDto());

        Page<ClienteResponseDTO> results = clienteService.findAll(ClienteFilter.builder().build(), pageable);

        assertThat(results.getNumber()).isEqualTo(1);
        assertThat(results.getTotalPages()).isEqualTo(2);
        assertThat(results.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldPassGivenFilterAndPageableToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        ClienteFilter filter = ClienteFilter.builder().firstName(FIRST_NAME).build();
        stubRepositoryPage(pageable, List.of(), 0);

        clienteService.findAll(filter, pageable);

        verify(clienteRepository).findAll(any(Specification.class), eq(pageable));
    }

    // ---- findById ----

    @Test
    void shouldReturnClientWhenIdExists() {
        Cliente entity = buildEntity();
        ClienteResponseDTO dto = buildResponseDto();
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(entity));
        when(clienteMapper.toResponseDTO(entity)).thenReturn(dto);

        ClienteResponseDTO result = clienteService.findById(CLIENTE_ID);

        assertThat(result).isEqualTo(dto);
        verify(clienteRepository).findById(CLIENTE_ID);
        verify(clienteMapper).toResponseDTO(entity);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenFindingUnknownId() {
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotCallMapperWhenFindingUnknownId() {
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.findById(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verifyNoInteractions(clienteMapper);
    }

    // ---- create ----

    @Test
    void shouldCreateClient() {
        ClienteRequestDTO requestDto = buildRequestDto();
        Cliente entityToSave = buildEntity();
        entityToSave.setId(null);
        Cliente savedEntity = buildEntity();
        ClienteResponseDTO responseDto = buildResponseDto();
        when(clienteMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(clienteRepository.save(entityToSave)).thenReturn(savedEntity);
        when(clienteMapper.toResponseDTO(savedEntity)).thenReturn(responseDto);

        ClienteResponseDTO result = clienteService.create(requestDto);

        assertThat(result).isEqualTo(responseDto);
        assertThat(meterRegistry.counter("clients.creations").count()).isEqualTo(1.0);
        verify(clienteMapper).toEntity(requestDto);
        verify(clienteRepository).save(entityToSave);
        verify(clienteMapper).toResponseDTO(savedEntity);
    }

    @Test
    void shouldReturnGeneratedIdAfterCreatingClient() {
        ClienteRequestDTO requestDto = buildRequestDto();
        Cliente entityToSave = buildEntity();
        entityToSave.setId(null);
        Cliente savedEntity = buildEntity();
        ClienteResponseDTO responseDto = buildResponseDto();
        when(clienteMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(clienteRepository.save(entityToSave)).thenReturn(savedEntity);
        when(clienteMapper.toResponseDTO(savedEntity)).thenReturn(responseDto);

        ClienteResponseDTO result = clienteService.create(requestDto);

        assertThat(result.getId()).isEqualTo(CLIENTE_ID);
    }

    @Test
    void shouldPersistEntityMappedFromRequestWhenCreatingClient() {
        ClienteRequestDTO requestDto = buildRequestDto();
        Cliente entityToSave = buildEntity();
        when(clienteMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(clienteRepository.save(entityToSave)).thenReturn(entityToSave);
        when(clienteMapper.toResponseDTO(entityToSave)).thenReturn(buildResponseDto());

        clienteService.create(requestDto);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(captor.getValue().getLastName()).isEqualTo(LAST_NAME);
        assertThat(captor.getValue().getEmail()).isEqualTo(EMAIL);
    }

    // ---- update ----

    @Test
    void shouldUpdateExistingClient() {
        Cliente existing = buildEntity();
        ClienteRequestDTO requestDto = buildRequestDtoWithUpdatedPhone();
        ClienteResponseDTO responseDto = buildResponseDto();
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(existing));
        when(clienteRepository.save(existing)).thenReturn(existing);
        when(clienteMapper.toResponseDTO(existing)).thenReturn(responseDto);

        ClienteResponseDTO result = clienteService.update(CLIENTE_ID, requestDto);

        assertThat(result).isEqualTo(responseDto);
        verify(clienteMapper).updateEntity(requestDto, existing);
        verify(clienteRepository).save(existing);
    }

    @Test
    void shouldDelegateFieldCopyingToMapperBeforeSaving() {
        Cliente existing = buildEntity();
        ClienteRequestDTO requestDto = buildRequestDto();
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            Cliente target = invocation.getArgument(1);
            target.setFirstName("Updated First");
            return null;
        }).when(clienteMapper).updateEntity(requestDto, existing);
        when(clienteRepository.save(existing)).thenReturn(existing);
        when(clienteMapper.toResponseDTO(existing)).thenReturn(buildResponseDto());

        clienteService.update(CLIENTE_ID, requestDto);

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getFirstName()).isEqualTo("Updated First");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingUnknownClient() {
        ClienteRequestDTO requestDto = buildRequestDto();
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(UNKNOWN_ID, requestDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotSaveWhenUpdatingUnknownClient() {
        ClienteRequestDTO requestDto = buildRequestDto();
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.update(UNKNOWN_ID, requestDto))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteMapper, never()).updateEntity(any(), any());
        verify(clienteRepository, never()).save(any());
    }

    // ---- delete ----

    @Test
    void shouldDeleteExistingClient() {
        Cliente existing = buildEntity();
        when(clienteRepository.findById(CLIENTE_ID)).thenReturn(Optional.of(existing));

        clienteService.delete(CLIENTE_ID);

        verify(clienteRepository).delete(existing);
        assertThat(meterRegistry.counter("clients.deleted").count()).isEqualTo(1.0);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingUnknownClient() {
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.delete(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(UNKNOWN_ID));
    }

    @Test
    void shouldNotDeleteWhenClientDoesNotExist() {
        when(clienteRepository.findById(UNKNOWN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.delete(UNKNOWN_ID))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clienteRepository, never()).delete(any(Cliente.class));
    }

    // ---- test data builders ----

    private Cliente buildEntity() {
        Cliente entity = new Cliente();
        entity.setId(CLIENTE_ID);
        entity.setFirstName(FIRST_NAME);
        entity.setLastName(LAST_NAME);
        entity.setEmail(EMAIL);
        entity.setPhone(PHONE);
        entity.setAddress(ADDRESS);
        entity.setCreatedAt(CREATED_AT);
        return entity;
    }

    private ClienteRequestDTO buildRequestDto() {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setEmail(EMAIL);
        dto.setPhone(PHONE);
        dto.setAddress(ADDRESS);
        return dto;
    }

    private ClienteRequestDTO buildRequestDtoWithUpdatedPhone() {
        ClienteRequestDTO dto = buildRequestDto();
        dto.setPhone("+34600111222");
        return dto;
    }

    private ClienteResponseDTO buildResponseDto() {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(CLIENTE_ID);
        dto.setFirstName(FIRST_NAME);
        dto.setLastName(LAST_NAME);
        dto.setEmail(EMAIL);
        dto.setPhone(PHONE);
        dto.setAddress(ADDRESS);
        dto.setCreatedAt(CREATED_AT);
        return dto;
    }
}
