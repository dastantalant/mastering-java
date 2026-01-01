package com.mastering.mega.config;

import com.mastering.mega.number.model.response.SearchResponse;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    @Bean
    public CsvMapper csvMapper() {
        return new CsvMapper();
    }

    @Bean
    public CsvSchema searchResponseCsvSchema(CsvMapper csvMapper) {
        return csvMapper.schemaFor(SearchResponse.class).withHeader();
    }
}
