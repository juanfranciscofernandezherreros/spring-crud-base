package com.example.crudbase.service;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;

import java.util.List;

public interface ResultService {

    List<ResultResponseDTO> findAll();

    ResultResponseDTO findById(Long id);

    ResultResponseDTO create(ResultRequestDTO dto);

    ResultResponseDTO update(Long id, ResultRequestDTO dto);

    void delete(Long id);
}
