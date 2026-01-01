package com.mastering.mega.number;


import com.mastering.mega.config.MegaProperties;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Component
@Slf4j
@Getter
public class MegaSessionManager {

    private volatile String cookieHeader = null;

    private final RestClient client;
    private final MegaProperties megaProperties;

    public MegaSessionManager(MegaProperties megaProperties) {
        client = RestClient.create();
        this.megaProperties = megaProperties;
    }

    public synchronized void refreshSession() {
        try {
            ResponseEntity<Void> response = client.get()
                    .uri(megaProperties.getSearchUri())
                    .retrieve()
                    .toBodilessEntity();

            List<String> setCookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
            if (setCookies == null || setCookies.isEmpty()) {
                throw new RuntimeException("Invalid Set-Cookie");
            }

            StringBuilder cookieBuilder = new StringBuilder();

            for (String setCookie : setCookies) {
                String valuePart = setCookie.split(";", 2)[0].trim();
                if (valuePart.startsWith("PHPSESSID=") || valuePart.matches("^_lang_\\d+_x=.+")) {
                    if (!cookieBuilder.isEmpty()) {
                        cookieBuilder.append("; ");
                    }
                    cookieBuilder.append(valuePart);
                }
            }

            if (cookieBuilder.isEmpty()) {
                throw new RuntimeException("Not found (PHPSESSID or _lang_)");
            }

            this.cookieHeader = cookieBuilder.toString();
            log.info("Success set session: {}", cookieHeader);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public String getCurrentCookie() {
        if (cookieHeader == null) {
            refreshSession();
        }
        return cookieHeader;
    }

    public void invalidateAndRefresh() {
        this.cookieHeader = null;
        refreshSession();
    }
}