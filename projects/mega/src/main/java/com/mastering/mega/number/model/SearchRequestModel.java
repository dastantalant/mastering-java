package com.mastering.mega.number.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestModel {

    public static final String CODE = "996";

    private int[] categories;

    @Builder.Default
    private int limit = 20;

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private String prefix = "___";

    @Builder.Default
    private String number = "XXXXXX";

    private String allCategories;

    public String getAllCategories() {
        if (allCategories == null) {
            StringBuilder sb = new StringBuilder();
            for (int category : categories) {
                sb.append(category).append(',');
            }
            allCategories = sb.toString();
        }
        return allCategories;
    }

    public void incrementPage() {
        page++;
    }
}
