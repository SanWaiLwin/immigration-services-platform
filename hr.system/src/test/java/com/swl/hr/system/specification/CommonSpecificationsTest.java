package com.swl.hr.system.specification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import static org.mockito.Mockito.*;

class CommonSpecificationsTest {

    @Nested
    @DisplayName("containsIgnoreCase")
    class ContainsIgnoreCase {
        @Test
        @DisplayName("returns null when value is null or blank")
        void returnsNullForNullOrBlank() {
            Specification<Object> s1 = CommonSpecifications.containsIgnoreCase("jobTitle", null);
            Specification<Object> s2 = CommonSpecifications.containsIgnoreCase("jobTitle", " ");
            assertNull(s1);
            assertNull(s2);
        }

        @Test
        @DisplayName("returns non-null specification when value is present")
        void returnsSpecWhenValuePresent() {
            Specification<Object> spec = CommonSpecifications.containsIgnoreCase("jobTitle", "Dev");
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("equalsIgnoreCase")
    class EqualsIgnoreCase {
        @Test
        @DisplayName("returns null when value is null or blank")
        void returnsNullForNullOrBlank() {
            Specification<Object> s1 = CommonSpecifications.equalsIgnoreCase("gender", null);
            Specification<Object> s2 = CommonSpecifications.equalsIgnoreCase("gender", " ");
            assertNull(s1);
            assertNull(s2);
        }

        @Test
        @DisplayName("returns non-null specification when value is present")
        void returnsSpecWhenValuePresent() {
            Specification<Object> spec = CommonSpecifications.equalsIgnoreCase("gender", "Female");
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("equal")
    class Equal {
        @Test
        @DisplayName("returns null when value is null")
        void returnsNullWhenNull() {
            Specification<Object> s1 = CommonSpecifications.equal("salary", null);
            assertNull(s1);
        }

        @Test
        @DisplayName("returns non-null specification when value is present")
        void returnsSpecWhenValuePresent() {
            Specification<Object> spec = CommonSpecifications.equal("salary", new BigDecimal("1000"));
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("gte")
    class Gte {
        @Test
        @DisplayName("returns null when value is null")
        void returnsNullWhenNull() {
            Specification<Object> s1 = CommonSpecifications.gte("salary", null);
            assertNull(s1);
        }

        @Test
        @DisplayName("returns non-null specification when value is present")
        void returnsSpecWhenValuePresent() {
            Specification<Object> spec = CommonSpecifications.gte("salary", new BigDecimal("500"));
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("lte")
    class Lte {
        @Test
        @DisplayName("returns null when value is null")
        void returnsNullWhenNull() {
            Specification<Object> s1 = CommonSpecifications.lte("salary", null);
            assertNull(s1);
        }

        @Test
        @DisplayName("returns non-null specification when value is present")
        void returnsSpecWhenValuePresent() {
            Specification<Object> spec = CommonSpecifications.lte("salary", new BigDecimal("2500"));
            assertNotNull(spec);
        }
    }

    @Nested
    @DisplayName("between")
    class Between {
        @Test
        @DisplayName("returns null when both from and to are null")
        void returnsNullWhenBothNull() {
            Specification<Object> s1 = CommonSpecifications.between("salary", null, null);
            assertNull(s1);
        }

        @Test
        @DisplayName("builds CriteriaBuilder.between when both from and to are present")
        void buildsBetweenPredicateWhenBothPresent() {
            BigDecimal from = new BigDecimal("500");
            BigDecimal to = new BigDecimal("2500");
            Specification<com.swl.hr.system.entity.JobData> spec = CommonSpecifications.between("salary", from, to);
            assertNotNull(spec);

            Root<com.swl.hr.system.entity.JobData> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path rawPath = mock(Path.class);
            @SuppressWarnings("unchecked")
            Path<BigDecimal> path = (Path<BigDecimal>) rawPath;
            Predicate expected = mock(Predicate.class);

            when(root.get("salary")).thenReturn(rawPath);
            when(cb.between(path, from, to)).thenReturn(expected);

            Predicate actual = spec.toPredicate(root, query, cb);
            assertEquals(expected, actual);
            verify(cb, times(1)).between(path, from, to);
            verify(cb, never()).greaterThanOrEqualTo(any(Path.class), any(BigDecimal.class));
            verify(cb, never()).lessThanOrEqualTo(any(Path.class), any(BigDecimal.class));
        }

        @Test
        @DisplayName("builds CriteriaBuilder.greaterThanOrEqualTo when only from is present")
        void buildsGtePredicateWhenOnlyFromPresent() {
            BigDecimal from = new BigDecimal("1000");
            Specification<com.swl.hr.system.entity.JobData> spec = CommonSpecifications.between("salary", from, null);
            assertNotNull(spec);

            Root<com.swl.hr.system.entity.JobData> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path rawPath = mock(Path.class);
            @SuppressWarnings("unchecked")
            Path<BigDecimal> path = (Path<BigDecimal>) rawPath;
            Predicate expected = mock(Predicate.class);

            when(root.get("salary")).thenReturn(rawPath);
            when(cb.greaterThanOrEqualTo(path, from)).thenReturn(expected);

            Predicate actual = spec.toPredicate(root, query, cb);
            assertEquals(expected, actual);
            verify(cb, times(1)).greaterThanOrEqualTo(path, from);
            verify(cb, never()).between(any(Path.class), any(BigDecimal.class), any(BigDecimal.class));
            verify(cb, never()).lessThanOrEqualTo(any(Path.class), any(BigDecimal.class));
        }

        @Test
        @DisplayName("builds CriteriaBuilder.lessThanOrEqualTo when only to is present")
        void buildsLtePredicateWhenOnlyToPresent() {
            BigDecimal to = new BigDecimal("3000");
            Specification<com.swl.hr.system.entity.JobData> spec = CommonSpecifications.between("salary", null, to);
            assertNotNull(spec);

            Root<com.swl.hr.system.entity.JobData> root = mock(Root.class);
            CriteriaQuery<?> query = mock(CriteriaQuery.class);
            CriteriaBuilder cb = mock(CriteriaBuilder.class);
            Path rawPath = mock(Path.class);
            @SuppressWarnings("unchecked")
            Path<BigDecimal> path = (Path<BigDecimal>) rawPath;
            Predicate expected = mock(Predicate.class);

            when(root.get("salary")).thenReturn(rawPath);
            when(cb.lessThanOrEqualTo(path, to)).thenReturn(expected);

            Predicate actual = spec.toPredicate(root, query, cb);
            assertEquals(expected, actual);
            verify(cb, times(1)).lessThanOrEqualTo(path, to);
            verify(cb, never()).between(any(Path.class), any(BigDecimal.class), any(BigDecimal.class));
            verify(cb, never()).greaterThanOrEqualTo(any(Path.class), any(BigDecimal.class));
        }
    }
}