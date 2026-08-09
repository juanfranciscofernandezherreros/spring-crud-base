package com.example.crudbase.mapper;

import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import com.example.crudbase.model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface ClienteMapper {

    /**
     * Converts a client request DTO into its corresponding entity.
     * The identifier and creation timestamp are server-generated, never
     * sourced from the request.
     *
     * @param dto the request data to convert
     * @return the resulting entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Cliente toEntity(ClienteRequestDTO dto);

    /**
     * Converts a client entity into its corresponding response DTO.
     *
     * @param entity the entity to convert
     * @return the resulting response DTO
     */
    ClienteResponseDTO toResponseDTO(Cliente entity);

    /**
     * Updates an existing Cliente entity in place with values from the request DTO.
     * The server-generated identifier and creation timestamp are never overwritten.
     *
     * @param dto the source data
     * @param entity the entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntity(ClienteRequestDTO dto, @MappingTarget Cliente entity);
}
