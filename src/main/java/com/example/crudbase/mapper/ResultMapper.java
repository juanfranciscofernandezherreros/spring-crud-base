package com.example.crudbase.mapper;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.model.Result;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface ResultMapper {

    /**
     * Converts a result request DTO into its corresponding entity.
     *
     * @param dto the request data to convert
     * @return the resulting entity
     */
    @Mapping(target = "id", ignore = true)
    Result toEntity(ResultRequestDTO dto);

    /**
     * Converts a result entity into its corresponding response DTO.
     *
     * @param entity the entity to convert
     * @return the resulting response DTO
     */
    ResultResponseDTO toResponseDTO(Result entity);

    /**
     * Updates an existing Result entity in place with values from the request DTO.
     * The server-generated identifier is never overwritten.
     *
     * @param dto the source data
     * @param entity the entity to update
     */
    @Mapping(target = "id", ignore = true)
    void updateEntity(ResultRequestDTO dto, @MappingTarget Result entity);
}
