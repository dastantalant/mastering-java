package com.mastering.mega.number.search;

import com.mastering.mega.number.model.response.SearchResponse;
import tools.jackson.databind.SequenceWriter;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvServiceImpl implements CsvService {
    @Qualifier("csvMapper")
    private final CsvMapper csvMapper;

    @Qualifier("searchResponseCsvSchema")
    private final CsvSchema schema;

    public void write(String prefix, List<SearchResponse> list) {
        File file = new File(prefix + ".csv");

        try (SequenceWriter writer = csvMapper.writer(schema).writeValues(file)) {
            writer.writeAll(list);
        }
    }
}
