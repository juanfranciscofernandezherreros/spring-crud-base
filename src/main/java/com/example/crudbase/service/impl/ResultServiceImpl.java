package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ResultFilter;
import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.exception.ResourceNotFoundException;
import com.example.crudbase.mapper.ResultMapper;
import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
import com.example.crudbase.repository.ResultSpecifications;
import com.example.crudbase.service.ResultService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public Page<ResultResponseDTO> findAll(ResultFilter filter, Pageable pageable) {
        Specification<Result> specification = ResultSpecifications.fromFilter(filter);
        return resultRepository.findAll(specification, pageable)
                .map(resultMapper::toResponseDTO);
    }

    @Override
    public ResultResponseDTO findById(Long id) {
        Result result = findResultOrThrow(id);
        return resultMapper.toResponseDTO(result);
    }

    @Override
    public ResultResponseDTO create(ResultRequestDTO dto) {
        Result result = resultMapper.toEntity(dto);
        Result saved = resultRepository.save(result);
        meterRegistry.counter("results.creations").increment();
        return resultMapper.toResponseDTO(saved);
    }

    @Override
    public ResultResponseDTO update(Long id, ResultRequestDTO dto) {
        Result existing = findResultOrThrow(id);
        resultMapper.updateEntity(dto, existing);
        Result updated = resultRepository.save(existing);
        return resultMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        Result existing = findResultOrThrow(id);
        resultRepository.delete(existing);
        meterRegistry.counter("results.deleted").increment();
    }

    private Result findResultOrThrow(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));
    }
}
