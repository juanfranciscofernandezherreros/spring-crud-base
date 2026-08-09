package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.exception.ResourceNotFoundException;
import com.example.crudbase.mapper.ResultMapper;
import com.example.crudbase.model.Result;
import com.example.crudbase.repository.ResultRepository;
import com.example.crudbase.service.ResultService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final ResultMapper resultMapper;

    public ResultServiceImpl(ResultRepository resultRepository, ResultMapper resultMapper) {
        this.resultRepository = resultRepository;
        this.resultMapper = resultMapper;
    }

    @Override
    public List<ResultResponseDTO> findAll() {
        return resultRepository.findAll().stream()
                .map(resultMapper::toResponseDTO)
                .toList();
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
        return resultMapper.toResponseDTO(saved);
    }

    @Override
    public ResultResponseDTO update(Long id, ResultRequestDTO dto) {
        Result existing = findResultOrThrow(id);
        existing.setHomeTeam(dto.getHomeTeam());
        existing.setAwayTeam(dto.getAwayTeam());
        existing.setHomeScore(dto.getHomeScore());
        existing.setAwayScore(dto.getAwayScore());
        existing.setMatchDate(dto.getMatchDate());
        existing.setCompetition(dto.getCompetition());
        existing.setVenue(dto.getVenue());
        Result updated = resultRepository.save(existing);
        return resultMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        Result existing = findResultOrThrow(id);
        resultRepository.delete(existing);
    }

    private Result findResultOrThrow(Long id) {
        return resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));
    }
}
