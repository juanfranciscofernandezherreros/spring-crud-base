package com.example.crudbase.mapper;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.model.Result;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResultMapper {

    Result toEntity(ResultRequestDTO dto);

    ResultResponseDTO toResponseDTO(Result entity);
}
