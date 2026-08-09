package com.example.crudbase.service.impl;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.dto.ClienteRequestDTO;
import com.example.crudbase.dto.ClienteResponseDTO;
import com.example.crudbase.exception.ResourceNotFoundException;
import com.example.crudbase.mapper.ClienteMapper;
import com.example.crudbase.model.Cliente;
import com.example.crudbase.repository.ClienteRepository;
import com.example.crudbase.repository.ClienteSpecifications;
import com.example.crudbase.service.ClienteService;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final MeterRegistry meterRegistry;

    @Override
    public Page<ClienteResponseDTO> findAll(ClienteFilter filter, Pageable pageable) {
        Specification<Cliente> specification = ClienteSpecifications.fromFilter(filter);
        return clienteRepository.findAll(specification, pageable)
                .map(clienteMapper::toResponseDTO);
    }

    @Override
    public ClienteResponseDTO findById(Long id) {
        Cliente cliente = findClienteOrThrow(id);
        return clienteMapper.toResponseDTO(cliente);
    }

    @Override
    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        Cliente cliente = clienteMapper.toEntity(dto);
        Cliente saved = clienteRepository.save(cliente);
        meterRegistry.counter("clients.creations").increment();
        return clienteMapper.toResponseDTO(saved);
    }

    @Override
    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        Cliente existing = findClienteOrThrow(id);
        clienteMapper.updateEntity(dto, existing);
        Cliente updated = clienteRepository.save(existing);
        return clienteMapper.toResponseDTO(updated);
    }

    @Override
    public void delete(Long id) {
        Cliente existing = findClienteOrThrow(id);
        clienteRepository.delete(existing);
        meterRegistry.counter("clients.deleted").increment();
    }

    private Cliente findClienteOrThrow(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
}
