package com.mastering.mega.number.search;

import com.mastering.mega.config.CategoryProperties;
import com.mastering.mega.config.MegaProperties;
import com.mastering.mega.config.PrefixProperties;
import com.mastering.mega.number.MegaSessionManager;
import com.mastering.mega.number.model.SearchRequestModel;
import com.mastering.mega.number.model.request.SearchRequest;
import com.mastering.mega.number.model.response.SearchResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.annotation.PostConstruct;

import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private static final ParameterizedTypeReference<List<SearchResponse>> TYPE = new ParameterizedTypeReference<>() {
    };

    private final CsvService csvService;
    private final MegaSessionManager sessionManager;

    private final MegaProperties megaProperties;
    private final CategoryProperties categoryProperties;
    private final PrefixProperties prefixProperties;

    @PostConstruct
    public void init() {
        log.info("Init session mega24.kg...");
        sessionManager.refreshSession();
        log.info("cookie: {}", sessionManager.getCurrentCookie());
    }

    public void run() {
        Iterator<String> prefixes = prefixProperties.getSet().iterator();

        SearchRequestModel model = SearchRequestModel.builder()
                .categories(categoryProperties.getAllCategories())
                .limit(megaProperties.getLimit())
                .page(1)
                .build();
        while (prefixes.hasNext()) {
            model.setPrefix(prefixes.next());
            processPrefix(model);
        }
    }

    private void processPrefix(SearchRequestModel model) {
        ResponseEntity<List<SearchResponse>> responseEntity = send(model);
        List<SearchResponse> response = responseEntity.getBody();

        while (responseEntity.getStatusCode().is2xxSuccessful()
                && response != null
                && !response.isEmpty()) {

            csvService.write(model.getPrefix(), response);
            model.incrementPage();

            responseEntity = send(model);
            response = responseEntity.getBody();
        }
    }

    private ResponseEntity<List<SearchResponse>> send(SearchRequestModel model) {
        final int MAX_ATTEMPTS = 3;
        SearchRequest request = SearchRequest.of(model);

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return sessionManager.getClient().post()
                        .uri(megaProperties.getSearchUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(request)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .toEntity(TYPE);

            } catch (HttpClientErrorException.BadRequest ex) {
                    if (attempt < MAX_ATTEMPTS) {
                        sessionManager.invalidateAndRefresh();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        continue;
                    }
                throw ex;

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                throw ex;
            }
        }

        throw new RuntimeException();
    }
}
