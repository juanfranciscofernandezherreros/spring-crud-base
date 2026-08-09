package com.example.crudbase.repository;

import com.example.crudbase.dto.ClienteFilter;
import com.example.crudbase.model.Cliente;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds a dynamic {@link Specification} from a {@link ClienteFilter}, matching only the
 * fields the caller actually set. String fields use a case-insensitive partial match;
 * identifier and timestamp fields use an exact match.
 */
public final class ClienteSpecifications {

    private ClienteSpecifications() {
    }

    public static Specification<Cliente> fromFilter(ClienteFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("firstName"), filter.getFirstName());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("lastName"), filter.getLastName());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("email"), filter.getEmail());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("phone"), filter.getPhone());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("address"), filter.getAddress());
            if (filter.getCreatedAt() != null) {
                predicates.add(criteriaBuilder.equal(root.get("createdAt"), filter.getCreatedAt()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void addContainsIgnoreCase(List<Predicate> predicates, CriteriaBuilder criteriaBuilder,
            Path<String> path, String value) {
        if (value != null && !value.isBlank()) {
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(path), "%" + value.toLowerCase(Locale.ROOT) + "%"));
        }
    }
}
