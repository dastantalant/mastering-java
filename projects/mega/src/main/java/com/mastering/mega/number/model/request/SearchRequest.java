package com.mastering.mega.number.model.request;

import com.mastering.mega.number.model.SearchRequestModel;

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
public class SearchRequest {
    private String categories; // 1,2,66,3,67,46,47,48,49
    @Builder.Default
    private int limit = 20; // 20
    @Builder.Default
    private int page = 1; // 1
    private String mask; // 996___X1111X

    public static SearchRequest of(SearchRequestModel model) {
        return SearchRequest.builder()
                .categories(model.getAllCategories())
                .limit(model.getLimit())
                .page(model.getPage())
                .mask(SearchRequestModel.CODE + model.getPrefix() + model.getNumber())
                .build();
    }
}