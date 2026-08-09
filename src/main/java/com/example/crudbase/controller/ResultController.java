package com.example.crudbase.controller;

import com.example.crudbase.dto.ResultFilter;
import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import com.example.crudbase.exception.ErrorResponse;
import com.example.crudbase.service.ResultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
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

/**
 * {@code @RestController} stays on the class because Spring MVC's handler
 * mapping requires it (or {@code @RequestMapping}) to recognize a bean as a
 * request handler, regardless of how the bean was instantiated. The bean
 * itself is registered explicitly in {@link com.example.crudbase.config.BeanConfig}
 * rather than discovered via component scanning — see
 * {@link com.example.crudbase.CrudBaseApplication} for the scan exclusion
 * that prevents Spring from also auto-registering a second instance.
 */
@RestController
@RequestMapping("/api/results")
@Tag(name = "Results", description = "Operations for managing match results")
@RequiredArgsConstructor
public class ResultController {

    private final ResultService resultService;

    @GetMapping
    @Operation(operationId = "getAllResults", summary = "Get a paginated list of results",
            description = "Returns match results matching the given filters (all optional), paginated and sortable. "
                    + "String filters match partially and case-insensitively; id, score, and date filters match exactly.")
    @ApiResponse(responseCode = "200", description = "Results retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PagedModel.class)))
    public ResponseEntity<PagedModel<ResultResponseDTO>> findAll(
            @ParameterObject ResultFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<ResultResponseDTO> page = resultService.findAll(filter, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getResultById", summary = "Get a result by id", description = "Returns the match result associated with the specified identifier.")
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
    @Operation(operationId = "createResult", summary = "Create a result", description = "Creates and persists a new match result.")
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
    @Operation(operationId = "updateResult", summary = "Update a result", description = "Fully updates an existing match result.")
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
    @Operation(operationId = "deleteResult", summary = "Delete a result", description = "Deletes the match result associated with the specified identifier.")
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
