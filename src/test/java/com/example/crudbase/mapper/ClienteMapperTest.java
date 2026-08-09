package com.example.crudbase.mapper;

import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import com.example.crudbase.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class ClienteMapperTest {

    private static final Long CLIENTE_ID = 1L;
    private static final Instant CREATED_AT = Instant.parse("2026-02-05T10:15:30Z");

    private ClienteMapper clienteMapper;

    @BeforeEach
    void setUp() {
        clienteMapper = new ClienteMapperImpl();
    }

    @Test
    void shouldMapRequestDtoFieldsToEntity() {
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Juan")
                .lastName("Fernandez")
                .email("juan.fernandez@example.com")
                .phone("+34600123456")
                .address("Calle Mayor 1, Madrid")
                .build();

        Cliente entity = clienteMapper.toEntity(dto);

        assertThat(entity.getFirstName()).isEqualTo("Juan");
        assertThat(entity.getLastName()).isEqualTo("Fernandez");
        assertThat(entity.getEmail()).isEqualTo("juan.fernandez@example.com");
        assertThat(entity.getPhone()).isEqualTo("+34600123456");
        assertThat(entity.getAddress()).isEqualTo("Calle Mayor 1, Madrid");
    }

    @Test
    void shouldIgnoreIdWhenMappingRequestDtoToNewEntity() {
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Juan")
                .lastName("Fernandez")
                .email("juan.fernandez@example.com")
                .build();

        Cliente entity = clienteMapper.toEntity(dto);

        assertThat(entity.getId()).isNull();
    }

    @Test
    void shouldIgnoreCreatedAtWhenMappingRequestDtoToNewEntity() {
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Juan")
                .lastName("Fernandez")
                .email("juan.fernandez@example.com")
                .build();

        Cliente entity = clienteMapper.toEntity(dto);

        assertThat(entity.getCreatedAt()).isNull();
    }

    @Test
    void shouldMapEntityToResponseDto() {
        Cliente entity = Cliente.builder()
                .id(CLIENTE_ID)
                .firstName("Juan")
                .lastName("Fernandez")
                .email("juan.fernandez@example.com")
                .phone("+34600123456")
                .address("Calle Mayor 1, Madrid")
                .createdAt(CREATED_AT)
                .build();

        ClienteResponseDTO dto = clienteMapper.toResponseDTO(entity);

        assertThat(dto.getId()).isEqualTo(CLIENTE_ID);
        assertThat(dto.getFirstName()).isEqualTo("Juan");
        assertThat(dto.getLastName()).isEqualTo("Fernandez");
        assertThat(dto.getEmail()).isEqualTo("juan.fernandez@example.com");
        assertThat(dto.getPhone()).isEqualTo("+34600123456");
        assertThat(dto.getAddress()).isEqualTo("Calle Mayor 1, Madrid");
        assertThat(dto.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    void shouldUpdateAllMutableFieldsOnExistingEntity() {
        Cliente existing = Cliente.builder()
                .id(CLIENTE_ID)
                .firstName("Old First")
                .lastName("Old Last")
                .email("old@example.com")
                .phone("111111111")
                .address("Old Address")
                .createdAt(CREATED_AT)
                .build();
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .email("updated@example.com")
                .phone("+34600999888")
                .address("Updated Address")
                .build();

        clienteMapper.updateEntity(dto, existing);

        assertThat(existing.getFirstName()).isEqualTo("Updated First");
        assertThat(existing.getLastName()).isEqualTo("Updated Last");
        assertThat(existing.getEmail()).isEqualTo("updated@example.com");
        assertThat(existing.getPhone()).isEqualTo("+34600999888");
        assertThat(existing.getAddress()).isEqualTo("Updated Address");
    }

    @Test
    void shouldNotOverwriteIdWhenUpdatingEntity() {
        Cliente existing = Cliente.builder()
                .id(CLIENTE_ID)
                .firstName("Old First")
                .lastName("Old Last")
                .email("old@example.com")
                .createdAt(CREATED_AT)
                .build();
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .email("updated@example.com")
                .build();

        clienteMapper.updateEntity(dto, existing);

        assertThat(existing.getId()).isEqualTo(CLIENTE_ID);
    }

    @Test
    void shouldNotOverwriteCreatedAtWhenUpdatingEntity() {
        Cliente existing = Cliente.builder()
                .id(CLIENTE_ID)
                .firstName("Old First")
                .lastName("Old Last")
                .email("old@example.com")
                .createdAt(CREATED_AT)
                .build();
        ClienteRequestDTO dto = ClienteRequestDTO.builder()
                .firstName("Updated First")
                .lastName("Updated Last")
                .email("updated@example.com")
                .build();

        clienteMapper.updateEntity(dto, existing);

        assertThat(existing.getCreatedAt()).isEqualTo(CREATED_AT);
    }
}
