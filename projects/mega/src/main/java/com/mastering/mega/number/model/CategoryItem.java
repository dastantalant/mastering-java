package com.mastering.mega.number.model;

import com.mastering.mega.util.NumberUtil;

import lombok.Builder;
import lombok.Getter;

@Builder
public record CategoryItem(@Getter int id, @Getter int price) {

    public static CategoryItem from(Category category) {
        if (category.getPrice() != null && NumberUtil.isValidIntegerWithSpaces(category.getPrice())) {
            return new CategoryItem(category.getId(), NumberUtil.parseIntWithSpaces(category.getPrice()));
        }
        return null;
    }

}