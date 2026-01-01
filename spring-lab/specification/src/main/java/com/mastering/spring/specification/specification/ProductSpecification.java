package com.mastering.spring.specification.specification;

import com.mastering.spring.specification.entity.Category;
import com.mastering.spring.specification.entity.Product;

import org.springframework.data.jpa.domain.Specification;

import lombok.experimental.UtilityClass;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

@UtilityClass
public class ProductSpecification {

    // #### EQUALITY
    public static Specification<Product> hasName(String name) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<Product> createdAt(Date date) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("createdAt"), date);
    }

    public static Specification<Product> hasNoName(String name) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.notEqual(root.get("name"), name);
    }
    // ####

    // #### STRINGS
    public static Specification<Product> hasNameLike(String name) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.like(root.get("name"), "%" + name + "%");
    }
    // ###

    // ### COMPARISONS
    public static Specification<Product> hasPriceGreaterThanOrEqualTo(BigDecimal price) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(root.get("price"), price);
    }

    public static Specification<Product> hasPriceLessThanOrEqualTo(BigDecimal price) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(root.get("name"), price);
    }

    public static Specification<Product> hasPriceBetween(BigDecimal min, BigDecimal max) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.between(root.get("price"), min, max);
    }

    public static Specification<Product> createdBeforeThan(Date date) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("createdAt"), date);
    }

    public static Specification<Product> createdAfterThan(Date date) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("createdAt"), date);
    }

    public static Specification<Product> updatedBeforeThan(Date date) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.lessThan(root.get("updatedAt"), date);
    }

    public static Specification<Product> updatedAfterThan(Date date) {
        return (root, _, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("updatedAt"), date);
    }
    // ###

    // ### JOINS
    public static Specification<Product> hasCategoryNameLike(String categoryName) {
        return (root, _, criteriaBuilder) -> {
            Join<Product, Category> categoryJoin = root.join("categories");

            return criteriaBuilder.like(categoryJoin.get("name"), categoryName);
        };
    }

    public static Specification<Product> hasCategoryIds(Collection<Long> categoryIds) {
        return (root, _, _) -> {
            Join<Product, Category> categoryJoin = root.join("categories");

            return categoryJoin.get("id").in(categoryIds);
        };
    }
    // ###

    // ### BOOLEANS
    public static Specification<Product> search(String keyword) {
        return (root, _, criteriaBuilder) -> {

            Expression<String> nameLowercase = criteriaBuilder.lower(root.get("name"));
            Expression<String> companyNameLowercase = criteriaBuilder.lower(root.get("companyName"));

            return criteriaBuilder.or(
                    criteriaBuilder.like(nameLowercase, "%" + keyword.toLowerCase() + "%"),
                    criteriaBuilder.like(companyNameLowercase, "%" + keyword.toLowerCase() + "%")
            );
        };
    }
    // ###

    // ### COLLECTION OPERATIONS
    public static Specification<Product> isCategorized() {
        return (root, _, criteriaBuilder) -> {
            Join<Product, Category> categoryJoin = root.join("categories");

            return criteriaBuilder.isNotEmpty(categoryJoin.get("id"));
        };
    }

    public static Specification<Product> hasCategoryIdsCountGreaterThan(int count) {
        return (root, _, criteriaBuilder) -> {
            Join<Product, Category> categoryJoin = root.join("categories");

            return criteriaBuilder.greaterThan(criteriaBuilder.size(categoryJoin.get("id")), count);
        };
    }
}
