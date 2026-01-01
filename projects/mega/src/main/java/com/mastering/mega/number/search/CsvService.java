package com.mastering.mega.number.search;

import com.mastering.mega.number.model.response.SearchResponse;

import java.util.List;

public interface CsvService {
    void write(String prefix, List<SearchResponse> list);
}
