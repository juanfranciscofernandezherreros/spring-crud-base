package com.example.crudbase.repository;

import com.example.crudbase.dto.ResultFilter;
import com.example.crudbase.model.Result;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds a dynamic {@link Specification} from a {@link ResultFilter}, matching only the
 * fields the caller actually set. String fields use a case-insensitive partial match;
 * identifier, numeric, and date fields use an exact match.
 */
public final class ResultSpecifications {

    private ResultSpecifications() {
    }

    public static Specification<Result> fromFilter(ResultFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("id"), filter.getId()));
            }
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("homeTeam"), filter.getHomeTeam());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("awayTeam"), filter.getAwayTeam());
            if (filter.getHomeScore() != null) {
                predicates.add(criteriaBuilder.equal(root.get("homeScore"), filter.getHomeScore()));
            }
            if (filter.getAwayScore() != null) {
                predicates.add(criteriaBuilder.equal(root.get("awayScore"), filter.getAwayScore()));
            }
            if (filter.getMatchDate() != null) {
                predicates.add(criteriaBuilder.equal(root.get("matchDate"), filter.getMatchDate()));
            }
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("competition"), filter.getCompetition());
            addContainsIgnoreCase(predicates, criteriaBuilder, root.get("venue"), filter.getVenue());

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
