package com.mastering.spring.specification.dto;

import com.mastering.spring.specification.entity.Product;
import com.mastering.spring.specification.specification.ProductSpecification;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
public class ListProductRequest implements Filterable<Product> {

    private Filter filter;

    @Getter
    @Setter
    private static class Filter {

        private String keyword;
        private String name;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Set<Long> categoryIds;
        private CreationDate date;

        @Getter
        @Setter
        private static class CreationDate {

            private Date createdAt;
            private DateComparison dateComparison;

            private enum DateComparison {
                AT,
                AFTER,
                BEFORE
            }
        }

    }

    @Override
    public Specification<Product> toSpecification() {
        Specification<Product> specification = Specification.unrestricted();

        if (filter.keyword != null) {
            specification = specification.and(ProductSpecification.search(filter.keyword));
        }

        if (filter.name != null) {
            specification = specification.and(ProductSpecification.hasName(filter.name));
        }

        if (filter.minPrice != null) {
            specification = specification.and(ProductSpecification.hasPriceGreaterThanOrEqualTo(filter.minPrice));
        }

        if (filter.maxPrice != null) {
            specification = specification.and(ProductSpecification.hasPriceLessThanOrEqualTo(filter.maxPrice));
        }

        if (!CollectionUtils.isEmpty(filter.categoryIds)) {
            specification = specification.and(ProductSpecification.hasCategoryIds(filter.categoryIds.stream().toList()));
        }

        if (filter.date != null && filter.date.createdAt != null && filter.date.dateComparison != null) {
            Date date = filter.date.createdAt;
            Filter.CreationDate.DateComparison dateComparison = filter.date.dateComparison;

            specification = switch (dateComparison) {
                case AT -> specification.and(ProductSpecification.createdAt(date));
                case AFTER -> specification.and(ProductSpecification.createdAfterThan(date));
                case BEFORE -> specification.and(ProductSpecification.createdBeforeThan(date));
            };
        }

        return specification;
    }

}
