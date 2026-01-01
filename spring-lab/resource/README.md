# Loading Resources in Spring Boot

This project demonstrates various ways to load resources in Spring Boot applications.

## Prerequisites

- Java 25
- Maven 3.x

## Running the Application

```bash
./mvnw spring-boot:run
```

---

## Example 1: Using @Value with Resource

The simplest way to load resources in Spring is by combining the `@Value` annotation with Spring's `Resource` interface.

### How It Works

Spring's `Resource` interface provides a unified abstraction for accessing low-level resources (files, URLs, classpath entries). 
When you use `@Value` with a resource path, Spring automatically creates the appropriate `Resource` implementation based on the **prefix** in your path.

### Resource Prefixes

| Prefix | Implementation | Use Case |
|--------|---------------|----------|
| `classpath:` | ClassPathResource | Files in `src/main/resources` |
| `file:` | FileSystemResource | Absolute filesystem paths |
| `file:./` | FileUrlResource | Relative to working directory |
| `https://` | UrlResource | Remote resources via HTTP/HTTPS |

### Code Example

```java
@RestController
public class ResourceController {

    // Load from classpath (src/main/resources/myFile.txt)
    @Value("classpath:myFile.txt")
    private Resource classpathResource;

    // Load from filesystem (relative to working directory)
    @Value("file:./data/config.txt")
    private Resource fileResource;

    // Load from remote URL
    @Value("https://example.com/data.json")
    private Resource urlResource;

    @GetMapping
    public String getContent() throws IOException {
        return new String(
            classpathResource.getContentAsByteArray(),
            StandardCharsets.UTF_8
        );
    }
}
```

### Common Mistake: Missing Prefix

```java
// This will FAIL - Spring doesn't know how to interpret the path
@Value("/myFile.txt")
private Resource resource;  // ERROR!

// This works - classpath prefix tells Spring where to look
@Value("classpath:myFile.txt")
private Resource resource;  // OK!
```

### Useful Resource Methods

The `Resource` interface provides helpful methods for working with resources:

```java
resource.exists()              // Check if the resource exists
resource.isReadable()          // Check if content can be read
resource.getFilename()         // Get the filename
resource.getDescription()      // Human-readable description
resource.getInputStream()      // Get an InputStream to read content
resource.getContentAsByteArray() // Read entire content as byte array
```

### Using Resource Implementations Directly

You can also instantiate Resource implementations directly instead of using `@Value`:

```java
// Create a ClassPathResource directly
Resource resource = new ClassPathResource("myFile.txt");
String content = new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8);
```

This is useful when:
- You need to create resources programmatically (e.g., in a loop)
- You're in a non-Spring-managed class where `@Value` isn't available
- You want explicit control over which Resource implementation to use

Available implementations: `ClassPathResource`, `FileSystemResource`, `UrlResource`, `ByteArrayResource`, etc.

### Test the Endpoints

Once the application is running, test these endpoints:

| Endpoint | Description |
|----------|-------------|
| `GET /` | Returns content from classpath resource (@Value) |
| `GET /classpath` | Returns content using ClassPathResource directly |
| `GET /file` | Returns content from filesystem resource |
| `GET /readme` | Returns content from remote URL resource |
| `GET /info` | Shows metadata for all resources |

```bash
# Test classpath resource
curl http://localhost:8080/

# Test filesystem resource
curl http://localhost:8080/file

# Test URL resource
curl http://localhost:8080/readme

# View resource metadata
curl http://localhost:8080/info
```

---

## Example 2: Using ResourceLoader

Use `ResourceLoader` when the resource path is determined at **runtime** rather than compile time.

### When to Use Each Approach

| Approach | Use When |
|----------|----------|
| `@Value` + Resource | Path is known at **compile time** |
| `ResourceLoader` | Path is determined at **runtime** |

### Why ResourceLoader?

With `@Value`, you need a separate field for every resource:

```java
@Value("classpath:json/users.json")
private Resource usersJson;

@Value("classpath:json/products.json")
private Resource productsJson;

// What if you have 50 files? 50 fields!
```

With `ResourceLoader`, you build the path dynamically:

```java
public String loadJson(String name) throws IOException {
    String path = "classpath:json/" + name + ".json";
    Resource resource = resourceLoader.getResource(path);
    return resource.getContentAsString(StandardCharsets.UTF_8);
}
```

### Code Example

```java
@RestController
@RequestMapping("/json")
public class ResourceLoaderController {

    private final ResourceLoader resourceLoader;

    // Inject via constructor
    public ResourceLoaderController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping("/{name}")
    public String getJsonFile(@PathVariable String name) throws IOException {
        // Path built at RUNTIME based on the request
        String path = "classpath:json/" + name + ".json";
        Resource resource = resourceLoader.getResource(path);

        if (!resource.exists()) {
            return "Resource not found: " + name + ".json";
        }

        return new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8);
    }
}
```

### Test the Endpoints

```bash
# Load users.json dynamically
curl http://localhost:8080/json/users

# Load products.json dynamically
curl http://localhost:8080/json/products
```

---

## Example 3: Using ResourcePatternResolver

Use `ResourcePatternResolver` when you need to load **multiple resources** matching a pattern.

### When to Use Each Approach

| Approach | Use When |
|----------|----------|
| `@Value` + Resource | Load ONE resource, path known at compile time |
| `ResourceLoader` | Load ONE resource, path determined at runtime |
| `ResourcePatternResolver` | Load MULTIPLE resources matching a pattern |

### Pattern Syntax

| Pattern | Matches |
|---------|---------|
| `classpath:sql/*.sql` | All `.sql` files in the `sql` folder |
| `classpath:config/**/*.xml` | All `.xml` files in `config` and subdirectories |
| `classpath*:META-INF/*.xml` | All `.xml` files from ALL JARs on the classpath |

The `*` matches files, `**` matches directories, and `classpath*:` searches across all JARs.

### Code Example

```java
@RestController
@RequestMapping("/sql")
public class ResourcePatternController {

    private final ResourcePatternResolver resourcePatternResolver;

    public ResourcePatternController(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @GetMapping
    public String getAllSqlFiles() throws IOException {
        // Load ALL .sql files matching the pattern
        Resource[] resources = resourcePatternResolver.getResources("classpath:sql/*.sql");

        StringBuilder result = new StringBuilder();
        for (Resource resource : resources) {
            result.append("=== ").append(resource.getFilename()).append(" ===\n");
            result.append(new String(resource.getContentAsByteArray(), StandardCharsets.UTF_8));
        }
        return result.toString();
    }
}
```

### Test the Endpoint

```bash
# Load all SQL migration files
curl http://localhost:8080/sql
```