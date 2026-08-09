package com.example.crudbase.service;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    /**
     * Retrieves a paginated page of clients, optionally narrowed down by any combination
     * of the given filters.
     *
     * @param filter   the criteria to filter clients by; fields left unset are not filtered on
     * @param pageable the requested page number, size, and sort order
     * @return the page of clients matching the filter
     */
    Page<ClienteResponseDTO> findAll(ClienteFilter filter, Pageable pageable);

    /**
     * Retrieves a client by its unique identifier.
     *
     * @param id the unique identifier of the client
     * @return the client associated with the specified identifier
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no client exists with the specified identifier
     */
    ClienteResponseDTO findById(Long id);

    /**
     * Creates and persists a new client.
     *
     * @param dto the data of the client to create
     * @return the newly created client
     */
    ClienteResponseDTO create(ClienteRequestDTO dto);

    /**
     * Updates an existing client.
     *
     * @param id the unique identifier of the client to update
     * @param dto the new data for the client
     * @return the updated client
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no client exists with the specified identifier
     */
    ClienteResponseDTO update(Long id, ClienteRequestDTO dto);

    /**
     * Deletes a client by its unique identifier.
     *
     * @param id the unique identifier of the client to delete
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no client exists with the specified identifier
     */
    void delete(Long id);
}
