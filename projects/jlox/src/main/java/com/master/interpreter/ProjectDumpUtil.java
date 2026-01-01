package com.master.interpreter;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectDumpUtil {

    // java com.platform.common.util.ProjectDumpUtil com.mycompany.service /Users/dev/myservice full_dump.md --gitignore --table-content --clipboard --minify
    public static void main(String[] args) {
        String pkg = args.length > 0 ? args[0] : "com.master.interpreter";
        Path root = args.length > 1 ? Paths.get(args[1]) : Paths.get(".");
        Path out = args.length > 2 ? Paths.get(args[2]) : Paths.get("project.md");

        Set<String> set = new HashSet<>(Arrays.asList(args));

        try {
            Config config = new Builder()
                    .root(root)
                    .targetPackage(pkg)
                    .outputPath(out)
                    .minimizeTokens(set.contains("--minify"))
                    .copyToClipboard(set.contains("--clipboard"))
                    .useGitIgnore(set.contains("--gitignore"))
                    .tableContent(set.contains("--table-content"))
                    .addExcludedDir("test")
                    .addExcludedFilename("ProjectDumpUtil.java")
                    .addExcludedPattern("*Test.java")
                    .addExcludedPattern("*.log")
//                    .addExcludedContent("^package .*")
//                    .addExcludedContent("^import .*")
                    .build();

            new ProjectDumpUtil(config).execute();
        } catch (Exception e) {
            System.err.println("❌ Critical Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private final Config config;

    public ProjectDumpUtil(Config config) {
        this.config = config;
    }

    public void execute() {
        System.out.println("📂 Project Root: " + config.rootPath());
        System.out.println("🔧 Mode: " + config.getModeDescription());

        try {
            List<Path> files = new FileScanner(config).scan();

            if (files.isEmpty()) {
                System.out.println("⚠️ No files found matching criteria.");
                return;
            }

            files.sort(Comparator.comparing(Path::toString));

            ReportResult result = new ReportWriter(config).write(files);

            System.out.println("✅ Saved to: " + config.outputPath().toAbsolutePath());
            System.out.println("📊 Total files: " + files.size());
            System.out.println("🔢 Approx Tokens: " + result.totalTokens());

            if (config.copyToClipboard()) {
                ClipboardHelper.copyToClipboard(config.outputPath());
            }

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to execute dump", e);
        }
    }

    private static class FileScanner {
        private final Config config;
        private final List<PathMatcher> gitIgnoreMatchers = new ArrayList<>();

        FileScanner(Config config) {
            this.config = config;
            if (config.useGitIgnore()) {
                loadGitIgnore();
            }
        }

        List<Path> scan() throws IOException {
            try (Stream<Path> stream = Files.walk(config.rootPath())) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(this::isNotTooLarge)
                        .filter(this::matchesTargetPackage)
                        .filter(this::isAllowedExtension)
                        .filter(this::isNotExcluded)
                        .collect(Collectors.toList());
            }
        }

        private boolean isNotTooLarge(Path path) {
            try {
                return Files.size(path) <= config.maxFileSize();
            } catch (IOException e) {
                return false;
            }
        }

        private boolean matchesTargetPackage(Path path) {
            String packagePath = config.targetPackage().replace(".", "/");
            return path.getParent().toString().replace(File.separator, "/").contains(packagePath);
        }

        private boolean isAllowedExtension(Path path) {
            return config.extensions().containsKey(getFileExtension(path));
        }

        private boolean isNotExcluded(Path path) {
            String filename = path.getFileName().toString();

            if (config.excludedFilenames().contains(filename)) return false;

            for (Path part : config.rootPath().relativize(path)) {
                if (config.excludedDirs().contains(part.toString())) return false;
            }

            if (config.excludedPatterns().stream().anyMatch(m -> m.matches(path.getFileName()))) return false;

            return !config.useGitIgnore() || !matchesGitIgnore(path);
        }

        private void loadGitIgnore() {
            Path gitIgnore = config.rootPath().resolve(".gitignore");
            if (!Files.exists(gitIgnore)) return;

            try {
                Files.readAllLines(gitIgnore).stream()
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(this::addGitIgnorePattern);
            } catch (IOException e) {
                System.err.println("⚠️ Failed to read .gitignore: " + e.getMessage());
            }
        }

        private void addGitIgnorePattern(String line) {
            String pattern = line;
            if (line.endsWith("/")) pattern = line + "**";
            if (!line.contains("/")) pattern = "**/" + line;
            else if (line.startsWith("/")) pattern = line.substring(1);

            try {
                gitIgnoreMatchers.add(FileSystems.getDefault().getPathMatcher("glob:" + pattern));
            } catch (IllegalArgumentException ignored) {
            }
        }

        private boolean matchesGitIgnore(Path path) {
            Path relative = config.rootPath().relativize(path);
            for (PathMatcher matcher : gitIgnoreMatchers) {
                if (matcher.matches(relative) || matcher.matches(path.getFileName())) return true;
            }
            return false;
        }
    }

    private static class ReportWriter {
        private final Config config;
        private final AtomicLong tokenCounter = new AtomicLong(0);
        private final ContentProcessor contentProcessor;

        ReportWriter(Config config) {
            this.config = config;
            contentProcessor = new ContentProcessor(config);
        }

        ReportResult write(List<Path> files) throws IOException {
            try (BufferedWriter writer = Files.newBufferedWriter(config.outputPath(), StandardCharsets.UTF_8)) {
                writeHeader(writer, files.size());

                if (config.tableContent()) {
                    writeTableOfContents(writer, files);
                }

                for (Path file : files) {
                    processFile(writer, file);
                }
            }
            return new ReportResult(tokenCounter.get());
        }

        private void writeHeader(BufferedWriter writer, int count) throws IOException {
            writer.write("# Project Context\n\n");
            writer.write("- **Package:** `" + config.targetPackage() + "`\n");
            writer.write("- **Generated:** " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n");
            writer.write("- **Files:** " + count + "\n\n---\n\n");
        }

        private void writeTableOfContents(BufferedWriter writer, List<Path> files) throws IOException {
            writer.write("## Table of Contents\n\n");
            for (Path file : files) {
                String rel = getRelativePath(config.rootPath(), file);
                String anchor = rel.toLowerCase().replaceAll("[^a-z0-9\\-_]", "").replace(" ", "-");
                writer.write("- [" + rel + "](#" + anchor + ")\n");
            }
            writer.write("\n---\n\n");
        }

        private void processFile(BufferedWriter writer, Path file) throws IOException {
            String relativePath = getRelativePath(config.rootPath(), file);
            String ext = getFileExtension(file);
            String lang = config.extensions().getOrDefault(ext, "text");

            writer.write("### 📄 `" + relativePath + "`\n\n");
            writer.write("```" + lang + "\n");

            try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
                lines.filter(contentProcessor::shouldKeepLine)
                        .map(contentProcessor::processLine)
                        .filter(Objects::nonNull)
                        .forEach(line -> writeLine(writer, line));
            } catch (UncheckedIOException | IOException e) {
                writer.write("// Error reading file: " + e.getMessage());
            }

            writer.write("```\n\n---\n\n");
        }

        private void writeLine(BufferedWriter writer, String line) {
            try {
                writer.write(line);
                writer.newLine();
                tokenCounter.addAndGet(line.length() / 4 + 1);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static class ContentProcessor {
        private final boolean minimize;
        private final List<Predicate<String>> exclusionRules;

        ContentProcessor(Config config) {
            minimize = config.minimizeTokens();
            exclusionRules = config.lineExclusionRules();
        }

        boolean shouldKeepLine(String line) {
            if (exclusionRules.isEmpty()) return true;
            for (Predicate<String> rule : exclusionRules) {
                if (rule.test(line)) return false;
            }
            return true;
        }

        String processLine(String line) {
            if (!minimize) return line;
            String trimmed = line.trim();
            return trimmed.isEmpty() ? null : line.replaceAll("\\s+$", "");
        }
    }

    private static class ClipboardHelper {
        static void copyToClipboard(Path path) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("⚠️ Cannot copy to clipboard: Headless environment.");
                return;
            }
            try {
                String content = Files.readString(path);
                StringSelection selection = new StringSelection(content);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
                System.out.println("📋 Copied to system clipboard!");
            } catch (Exception e) {
                System.err.println("⚠️ Failed to copy to clipboard: " + e.getMessage());
            }
        }
    }

    public record ReportResult(long totalTokens) {
    }

    public record Config(
            Path rootPath,
            String targetPackage,
            Path outputPath,
            long maxFileSize,
            boolean minimizeTokens,
            boolean copyToClipboard,
            boolean useGitIgnore,
            boolean tableContent,
            Set<String> excludedDirs,
            Set<String> excludedFilenames,
            List<PathMatcher> excludedPatterns,
            List<Predicate<String>> lineExclusionRules,
            Map<String, String> extensions
    ) {
        public String getModeDescription() {
            return (minimizeTokens ? "Minified" : "Full") +
                    (useGitIgnore ? " + .gitignore" : "") +
                    (copyToClipboard ? " + Clipboard" : "");
        }
    }

    public static class Builder {
        private Path rootPath = Paths.get(".");
        private String targetPackage = "";
        private Path outputPath = Paths.get("project.md");
        private final long maxFileSize = 1024 * 1024;
        private boolean minimizeTokens = false;
        private boolean copyToClipboard = false;
        private boolean useGitIgnore = false;
        private boolean tableContent = false;

        private final Set<String> excludedDirs = new HashSet<>(Set.of(
                "target", ".git", ".idea", ".mvn", "node_modules", "build", "dist", ".gradle", "out", "bin"));
        private final Set<String> excludedFilenames = new HashSet<>();
        private final List<String> rawExcludedPatterns = new ArrayList<>();
        private final List<String> rawExcludedContent = new ArrayList<>();

        private final Map<String, String> extensions = new HashMap<>(Map.ofEntries(
                Map.entry(".java", "java"),
                Map.entry(".kt", "kotlin"),
                Map.entry(".xml", "xml"),
                Map.entry(".yaml", "yaml"),
                Map.entry(".yml", "yaml"),
                Map.entry(".json", "json"),
                Map.entry(".sql", "sql"),
                Map.entry(".properties", "properties"),
                Map.entry(".js", "javascript"),
                Map.entry(".ts", "typescript"),
                Map.entry(".html", "html"),
                Map.entry(".css", "css"),
                Map.entry(".md", "markdown"),
                Map.entry(".gitignore", "text")
        ));

        public Builder root(Path root) {
            rootPath = resolveProjectRoot(root);
            return this;
        }

        public Builder targetPackage(String pkg) {
            targetPackage = pkg;
            return this;
        }

        public Builder outputPath(Path out) {
            outputPath = out;
            return this;
        }

        public Builder minimizeTokens(boolean val) {
            minimizeTokens = val;
            return this;
        }

        public Builder copyToClipboard(boolean val) {
            copyToClipboard = val;
            return this;
        }

        public Builder useGitIgnore(boolean val) {
            useGitIgnore = val;
            return this;
        }

        public Builder tableContent(boolean val) {
            tableContent = val;
            return this;
        }

        public Builder addExcludedFilename(String f) {
            excludedFilenames.add(f);
            return this;
        }

        public Builder addExcludedDir(String d) {
            excludedDirs.add(d);
            return this;
        }

        public Builder addExcludedPattern(String p) {
            rawExcludedPatterns.add(p);
            return this;
        }

        public Builder addExcludedContent(String c) {
            rawExcludedContent.add(c);
            return this;
        }

        public Config build() {
            if (targetPackage.isEmpty()) throw new IllegalArgumentException("Target package required");

            List<PathMatcher> compiledPatterns = rawExcludedPatterns.stream()
                    .map(p -> FileSystems.getDefault().getPathMatcher("glob:" + p))
                    .collect(Collectors.toList());

            List<Predicate<String>> compiledContentRules = rawExcludedContent.stream()
                    .map(this::compileContentRule)
                    .collect(Collectors.toList());

            return new Config(rootPath, targetPackage, outputPath, maxFileSize, minimizeTokens,
                    copyToClipboard, useGitIgnore, tableContent, excludedDirs, excludedFilenames,
                    compiledPatterns, compiledContentRules, extensions);
        }

        private Predicate<String> compileContentRule(String rule) {
            if (rule.startsWith("^") || rule.endsWith("$") || rule.contains(".*")) {
                Pattern p = Pattern.compile(rule);
                return s -> p.matcher(s).find();
            }
            return s -> s.contains(rule);
        }

        private static Path resolveProjectRoot(Path start) {
            Path current = start.toAbsolutePath().normalize();
            while (current != null) {
                if (Files.exists(current.resolve("pom.xml")) ||
                        Files.exists(current.resolve("build.gradle")) ||
                        Files.exists(current.resolve(".git"))) {
                    return current;
                }
                current = current.getParent();
            }
            return start.toAbsolutePath();
        }
    }

    private static String getFileExtension(Path file) {
        String name = file.getFileName().toString();
        int idx = name.lastIndexOf('.');
        return idx == -1 ? "" : name.substring(idx);
    }

    private static String getRelativePath(Path root, Path file) {
        return root.relativize(file).toString().replace("\\", "/");
    }
}