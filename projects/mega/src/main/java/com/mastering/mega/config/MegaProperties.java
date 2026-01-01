package com.mastering.mega.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties("mega")
public class MegaProperties {
    private String host;
    private String searchRoute;
    private int limit;

    private String searchUri;

    public String getSearchUri() {
        if (searchUri == null) {
            searchUri = host + searchRoute;
        }
        return searchUri;
    }
}
