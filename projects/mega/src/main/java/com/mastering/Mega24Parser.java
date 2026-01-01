package com.mastering;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mega24Parser {

    // private static final String[] PREFIXES = {"550", "551", "552", "553", "554", "555", "556", "557", "558", "559", "755", "880", "888", "990", "995", "997", "998", "999"};
    private static final String[] PREFIXES = {"555"};
    private static final int LIMIT = 20000;
    private static final String URL = "https://mega24.kg/ru/number/search";

    private static final String CSV_HEADER = "MSISDN,CATEGORY_NAME,CATEGORY_PRICE,NCLS_ID,NSTS_ID";

    private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private static final HttpClient client = HttpClient.newBuilder()
            .cookieHandler(cookieManager)
            .build();

    public static void main(String[] args) {
        System.out.println("Запуск парсера Mega24 (Java)...");

        try {
            HttpRequest initRequest = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .GET()
                    .build();
            client.send(initRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Session initialized");
        } catch (Exception e) {
            System.err.println("Initialization error: " + e.getMessage());
            return;
        }

        for (String prefix : PREFIXES) {
            processPrefix(prefix);
        }

        System.out.println("All prefixes processed");
    }

    private static void processPrefix(String prefix) {
        System.out.println("\n>>> PREFIX PROCESSING: " + prefix + " <<<");
        String mask = "996" + prefix + "XXXXXX";
        int page = 1;

        while (true) {
            try {
                System.out.print("Loading: " + prefix + ", page " + page + "... ");

                String payload = String.format(
                        "{\"category\": \"1,2,66,3,67,46,47,48,49\", \"limit\": %d, \"page\": %d, \"mask\": \"%s\"}",
                        LIMIT, page, mask
                );

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "Java-Mega24-Client")
                        .POST(HttpRequest.BodyPublishers.ofString(payload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    System.out.println("Error HTTP " + response.statusCode() + ". Wait 5 seconds...");
                    Thread.sleep(5000);
                    continue;
                }

                String jsonResponse = response.body();

                if (jsonResponse == null || jsonResponse.trim().equals("[]") || jsonResponse.length() < 10) {
                    System.out.println("The data has run out");
                    break;
                }

                Map<String, List<String>> groupedData = parseAndGroupJson(jsonResponse);

                if (groupedData.isEmpty()) {
                    System.out.println("No valid objects found (or end of data)");
                    break;
                }

                for (Map.Entry<String, List<String>> entry : groupedData.entrySet()) {
                    String nclsId = entry.getKey();
                    List<String> rows = entry.getValue();
                    writeToSpecificFile(prefix, nclsId, rows);
                }

                System.out.println("Done. Records processed: " + groupedData.values().stream().mapToInt(List::size).sum());

                page++;
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("\nCritical error: " + e.getMessage());
                e.printStackTrace();
                break;
            }
        }
    }

    private static Map<String, List<String>> parseAndGroupJson(String json) {
        Map<String, List<String>> groups = new HashMap<>();

        Pattern objectPattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher objectMatcher = objectPattern.matcher(json);

        while (objectMatcher.find()) {
            String objectContent = objectMatcher.group(1);

            String msisdn = extractValue(objectContent, "MSISDN");
            String catName = extractValue(objectContent, "CATEGORY_NAME");
            String catPrice = extractValue(objectContent, "CATEGORY_PRICE");
            String nclsId = extractValue(objectContent, "NCLS_ID");
            String nstsId = extractValue(objectContent, "NSTS_ID");

            if (nclsId == null) nclsId = "unknown";

            if (catName != null && catName.contains(",")) {
                catName = "\"" + catName + "\"";
            }

            String csvRow = String.join(",",
                    msisdn,
                    catName,
                    catPrice,
                    nclsId,
                    nstsId
            );

            groups.computeIfAbsent(nclsId, k -> new ArrayList<>()).add(csvRow);
        }

        return groups;
    }

    private static String extractValue(String source, String key) {
        Pattern p = Pattern.compile("\"" + key + "\":\\s*\"?([^,\"]*)\"?");
        Matcher m = p.matcher(source);
        if (m.find()) {
            return decodeUnicode(m.group(1));
        }
        return "";
    }

    private static String decodeUnicode(String val) {
        if (val == null) return null;
        if (!val.contains("\\u")) return val;

        StringBuilder sb = new StringBuilder();
        int len = val.length();
        for (int i = 0; i < len; i++) {
            char ch = val.charAt(i);
            if (ch == '\\' && i + 5 < len && val.charAt(i + 1) == 'u') {
                String hex = val.substring(i + 2, i + 6);
                try {
                    sb.append((char) Integer.parseInt(hex, 16));
                    i += 5;
                } catch (NumberFormatException e) {
                    sb.append(ch);
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private static void writeToSpecificFile(String prefix, String nclsId, List<String> rows) throws IOException {
        String filename = "mega24_" + prefix + "_" + nclsId + ".csv";
        Path path = Paths.get(filename);
        boolean fileExists = Files.exists(path);

        try (BufferedWriter writer = Files.newBufferedWriter(
                path,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {

            if (!fileExists) {
                writer.write('\ufeff');
                writer.write(CSV_HEADER);
                writer.newLine();
            }

            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
}