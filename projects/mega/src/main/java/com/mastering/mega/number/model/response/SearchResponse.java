package com.mastering.mega.number.model.response;

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
public class SearchResponse {
    private String MSISDN;
    private String NCLS_ID;
    private String NSTS_ID;
    private String CATEGORY_PRICE;
    private String CATEGORY_NAME;
}
