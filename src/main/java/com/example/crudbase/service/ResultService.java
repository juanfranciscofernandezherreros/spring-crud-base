package com.example.crudbase.service;

import com.example.crudbase.dto.ResultFilter;
import com.example.crudbase.dto.ResultRequestDTO;
import com.example.crudbase.dto.ResultResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ResultService {

    /**
     * Retrieves a paginated page of results, optionally narrowed down by any combination
     * of the given filters.
     *
     * @param filter   the criteria to filter results by; fields left unset are not filtered on
     * @param pageable the requested page number, size, and sort order
     * @return the page of results matching the filter
     */
    Page<ResultResponseDTO> findAll(ResultFilter filter, Pageable pageable);

    /**
     * Retrieves a result by its unique identifier.
     *
     * @param id the unique identifier of the result
     * @return the result associated with the specified identifier
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no result exists with the specified identifier
     */
    ResultResponseDTO findById(Long id);

    /**
     * Creates and persists a new result.
     *
     * @param dto the data of the result to create
     * @return the newly created result
     */
    ResultResponseDTO create(ResultRequestDTO dto);

    /**
     * Updates an existing result.
     *
     * @param id the unique identifier of the result to update
     * @param dto the new data for the result
     * @return the updated result
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no result exists with the specified identifier
     */
    ResultResponseDTO update(Long id, ResultRequestDTO dto);

    /**
     * Deletes a result by its unique identifier.
     *
     * @param id the unique identifier of the result to delete
     * @throws com.example.crudbase.exception.ResourceNotFoundException if no result exists with the specified identifier
     */
    void delete(Long id);
}
