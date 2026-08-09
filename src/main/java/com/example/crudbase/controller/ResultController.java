package com.example.crudbase.controller;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.service.ResultService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    public ResponseEntity<List<ResultResponseDTO>> findAll() {
        return ResponseEntity.ok(resultService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResultResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(resultService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResultResponseDTO> create(@Valid @RequestBody ResultRequestDTO dto) {
        ResultResponseDTO created = resultService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResultResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ResultRequestDTO dto) {
        return ResponseEntity.ok(resultService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
