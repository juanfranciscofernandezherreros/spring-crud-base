package com.example.crudbase.controller;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import com.example.crudbase.exception.ErrorResponse;
import com.example.crudbase.service.ClienteService;
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
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Operations for managing clients")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(operationId = "listClients", summary = "Get a paginated list of clients",
            description = "Returns clients matching the given filters (all optional), paginated and sortable. "
                    + "String filters match partially and case-insensitively; id and creation timestamp filters match exactly.")
    @ApiResponse(responseCode = "200", description = "Clients retrieved successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PagedModel.class)))
    public ResponseEntity<PagedModel<ClienteResponseDTO>> findAll(
            @ParameterObject ClienteFilter filter,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<ClienteResponseDTO> page = clienteService.findAll(filter, pageable);
        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getClientById", summary = "Get a client by id", description = "Returns the client associated with the specified identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> findById(
            @Parameter(description = "Unique identifier of the client", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.findById(id));
    }

    @PostMapping
    @Operation(operationId = "createClient", summary = "Create a client", description = "Creates and persists a new client.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Client created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO dto) {
        ClienteResponseDTO created = clienteService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateClient", summary = "Update a client", description = "Fully updates an existing client.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Client updated",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ClienteResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ClienteResponseDTO> update(
            @Parameter(description = "Unique identifier of the client to update", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(operationId = "deleteClient", summary = "Delete a client", description = "Deletes the client associated with the specified identifier.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Client deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "Client not found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique identifier of the client to delete", example = "1", required = true)
            @PathVariable Long id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
