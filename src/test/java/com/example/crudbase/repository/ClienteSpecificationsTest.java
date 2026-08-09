package com.example.crudbase.repository;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClienteSpecificationsTest {

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente juan;
    private Cliente maria;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
        juan = clienteRepository.save(Cliente.builder()
                .firstName("Juan")
                .lastName("Fernandez")
                .email("juan.fernandez@example.com")
                .phone("+34600123456")
                .address("Calle Mayor 1, Madrid")
                .build());
        maria = clienteRepository.save(Cliente.builder()
                .firstName("Maria")
                .lastName("Lopez")
                .email("maria.lopez@example.com")
                .phone("+34600987654")
                .address("Avenida Diagonal 2, Barcelona")
                .build());
    }

    private List<Cliente> filter(ClienteFilter filter) {
        Pageable pageable = PageRequest.of(0, 20);
        return clienteRepository.findAll(ClienteSpecifications.fromFilter(filter), pageable).getContent();
    }

    @Test
    void shouldReturnAllClientsWhenFilterIsEmpty() {
        List<Cliente> results = filter(ClienteFilter.builder().build());

        assertThat(results).containsExactlyInAnyOrder(juan, maria);
    }

    @Test
    void shouldFilterByExactId() {
        List<Cliente> results = filter(ClienteFilter.builder().id(juan.getId()).build());

        assertThat(results).containsExactly(juan);
    }

    @Test
    void shouldFilterByFirstNamePartialCaseInsensitiveMatch() {
        List<Cliente> results = filter(ClienteFilter.builder().firstName("jua").build());

        assertThat(results).containsExactly(juan);
    }

    @Test
    void shouldFilterByLastNamePartialCaseInsensitiveMatch() {
        List<Cliente> results = filter(ClienteFilter.builder().lastName("lopez").build());

        assertThat(results).containsExactly(maria);
    }

    @Test
    void shouldFilterByEmailPartialCaseInsensitiveMatch() {
        List<Cliente> results = filter(ClienteFilter.builder().email("MARIA.LOPEZ").build());

        assertThat(results).containsExactly(maria);
    }

    @Test
    void shouldFilterByPhonePartialMatch() {
        List<Cliente> results = filter(ClienteFilter.builder().phone("987654").build());

        assertThat(results).containsExactly(maria);
    }

    @Test
    void shouldFilterByAddressPartialCaseInsensitiveMatch() {
        List<Cliente> results = filter(ClienteFilter.builder().address("madrid").build());

        assertThat(results).containsExactly(juan);
    }

    @Test
    void shouldFilterByExactCreatedAt() {
        Cliente withKnownTimestamp = clienteRepository.save(Cliente.builder()
                .firstName("Carlos")
                .lastName("Ruiz")
                .email("carlos.ruiz@example.com")
                .createdAt(Instant.parse("2020-01-01T00:00:00Z"))
                .build());

        List<Cliente> results = filter(ClienteFilter.builder().createdAt(withKnownTimestamp.getCreatedAt()).build());

        assertThat(results).containsExactly(withKnownTimestamp);
    }

    @Test
    void shouldCombineMultipleFiltersWithAnd() {
        List<Cliente> results = filter(ClienteFilter.builder().firstName("Juan").lastName("Fernandez").build());

        assertThat(results).containsExactly(juan);
    }

    @Test
    void shouldReturnNoResultsWhenCombinedFiltersDoNotMatchTheSameRecord() {
        List<Cliente> results = filter(ClienteFilter.builder().firstName("Juan").lastName("Lopez").build());

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnNoResultsWhenNoRecordMatchesTheFilter() {
        List<Cliente> results = filter(ClienteFilter.builder().firstName("Nonexistent").build());

        assertThat(results).isEmpty();
    }
}
