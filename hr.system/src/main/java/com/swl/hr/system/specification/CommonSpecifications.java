package com.swl.hr.system.specification;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

public final class CommonSpecifications {

    private CommonSpecifications() {}

    public static <T> Specification<T> containsIgnoreCase(String field, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        String pattern = "%" + value.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get(field)), pattern);
    }

    public static <T> Specification<T> equalsIgnoreCase(String field, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return (root, query, cb) -> cb.equal(cb.lower(root.get(field)), value.trim().toLowerCase());
    }

    public static <T> Specification<T> equal(String field, BigDecimal value) {
        if (value == null) return null;
        return (root, query, cb) -> cb.equal(root.get(field), value);
    }

    public static <T> Specification<T> gte(String field, BigDecimal value) {
        if (value == null) return null;
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> lte(String field, BigDecimal value) {
        if (value == null) return null;
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(field), value);
    }

    public static <T> Specification<T> between(String field, BigDecimal from, BigDecimal to) {
        if (from == null && to == null) return null;
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.between(root.get(field), from, to);
            } else if (from != null) {
                return cb.greaterThanOrEqualTo(root.get(field), from);
            } else {
                return cb.lessThanOrEqualTo(root.get(field), to);
            }
        };
    }
}