package com.mastering.spring.resource;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Example 3: Loading Multiple Resources with ResourcePatternResolver
 * <p>
 * When to use ResourcePatternResolver instead of ResourceLoader?<br/>
 * ---------------------------------------------------------------<br/>
 * Use ResourceLoader:          Load ONE resource by exact path<br/>
 * Use ResourcePatternResolver: Load MULTIPLE resources matching a pattern
 * <p>
 * Pattern Syntax:<br/>
 * - classpath:sql/*.sql        → All .sql files in the sql folder<br/>
 * - classpath:config/**\/*.xml  → All .xml files in config and subdirectories<br/>
 * - classpath*:META-INF/*.xml  → All .xml files from ALL JARs on the classpath
 * <p>
 * Common use cases:<br/>
 * - Loading all database migration scripts<br/>
 * - Processing all configuration files in a directory<br/>
 * - Scanning for plugin descriptors across multiple JARs
 */
@RestController
@RequestMapping("/sql")
public class ResourcePatternController {

    private final ResourcePatternResolver resourcePatternResolver;

    public ResourcePatternController(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * GET /sql<br/>
     * Load ALL SQL files matching the pattern "classpath:sql/*.sql"
     * <p>
     * This demonstrates loading multiple resources at once - something<br/>
     * you can't do with ResourceLoader or @Value.
     */
    @GetMapping
    public String getAllSqlFiles() throws IOException {
        Resource[] resources = resourcePatternResolver.getResources("classpath:sql/*.sql");

        // Sort by filename to ensure consistent ordering
        Arrays.sort(resources, (a, b) -> {
            String nameA = a.getFilename() != null ? a.getFilename() : "";
            String nameB = b.getFilename() != null ? b.getFilename() : "";
            return nameA.compareTo(nameB);
        });

        StringBuilder result = new StringBuilder();
        result.append("Found ").append(resources.length).append(" SQL files:\n\n");

        for (Resource resource : resources) {
            result.append("=== ").append(resource.getFilename()).append(" ===\n");
            result.append(new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8));
            result.append("\n");
        }

        return result.toString();
    }

}
