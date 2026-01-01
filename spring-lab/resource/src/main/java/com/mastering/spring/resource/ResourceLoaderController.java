package com.mastering.spring.resource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Example 2: Loading Resources with ResourceLoader
 * <p>
 * When to use ResourceLoader instead of @Value?<br>
 * ---------------------------------------------<br>
 * Use @Value when:      The resource path is known at COMPILE TIME<br>
 * Use ResourceLoader:   The resource path is determined at RUNTIME
 * <p>
 * ResourceLoader is ideal when:<br>
 * - The path depends on user input or request parameters<br>
 * - You need to load one of many possible files dynamically<br>
 * - The resource location is computed or configured at runtime
 * <p>
 * Example: Loading different JSON files based on the request<br>
 * With @Value, you'd need a separate field for EVERY possible file.<br>
 * With ResourceLoader, you build the path dynamically.
 */
@RestController
@RequestMapping("/json")
@RequiredArgsConstructor
public class ResourceLoaderController {

    private final ResourceLoader resourceLoader;

    /**
     * GET /json/{name}<br>
     * Load a JSON file dynamically based on the name parameter.
     * <p>
     * This demonstrates the key advantage of ResourceLoader:<br>
     * the path "classpath:json/{name}.json" is built at RUNTIME<br>
     * based on the request, not hardcoded at compile time.
     * <p>
     * Try: /json/users or /json/products
     */
    @GetMapping("/{name}")
    public String getJsonFile(@PathVariable String name) throws IOException {
        // Build the resource path dynamically - this is why we use ResourceLoader!
        String path = "classpath:json/" + name + ".json";
        Resource resource = resourceLoader.getResource(path);

        if (!resource.exists()) {
            return "Resource not found: " + name + ".json";
        }

        return new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8);
    }

}
