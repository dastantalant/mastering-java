package com.mastering.mega;


import com.mastering.mega.number.search.SearchServiceImpl;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchRunner implements ApplicationRunner {
    private final SearchServiceImpl searchService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        searchService.run();
    }
}
