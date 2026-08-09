package com.example.crudbase.controller;

import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.exception.ErrorResponse;
import com.example.crudbase.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Results", description = "Operations for managing match results")
public class ResultController {

    private final ResultService resultService;

    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }

    @GetMapping
    @Operation(summary = "Get all results", description = "Returns every match result currently stored.")
    @ApiResponse(responseCode = "200", description = "Results retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(implementation = ResultResponseDTO.class))))
    public ResponseEntity<List<ResultResponseDTO>> findAll() {
        return ResponseEntity.ok(resultService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a result by id", description = "Returns the match result associated with the specified identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Result found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResultResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Result not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultResponseDTO> findById(
            @Parameter(description = "Unique identifier of the result", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(resultService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Create a result", description = "Creates and persists a new match result.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Result created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResultResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultResponseDTO> create(@Valid @RequestBody ResultRequestDTO dto) {
        ResultResponseDTO created = resultService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a result", description = "Fully updates an existing match result.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Result updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ResultResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Result not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ResultResponseDTO> update(
            @Parameter(description = "Unique identifier of the result to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ResultRequestDTO dto) {
        return ResponseEntity.ok(resultService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a result", description = "Deletes the match result associated with the specified identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Result deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Result not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the result to delete", example = "1", required = true)
            @PathVariable Long id) {
        resultService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
