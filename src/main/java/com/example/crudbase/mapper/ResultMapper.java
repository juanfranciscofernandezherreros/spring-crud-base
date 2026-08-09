package com.example.crudbase.mapper;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.model.Result;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    /**
     * Converts a result request DTO into its corresponding entity.
     *
     * @param dto the request data to convert
     * @return the resulting entity
     */
    Result toEntity(ResultRequestDTO dto);

    /**
     * Converts a result entity into its corresponding response DTO.
     *
     * @param entity the entity to convert
     * @return the resulting response DTO
     */
    ResultResponseDTO toResponseDTO(Result entity);
}
