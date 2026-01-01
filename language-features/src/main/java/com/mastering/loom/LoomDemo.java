package com.mastering.loom;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.LongAdder;

public class LoomDemo {

    // Test Parameters
     static final int TASK_COUNT = 100_000;
     static final int TASK_DELAY_MS = 100;

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    public static void main(String[] args) throws InterruptedException {
        printHeader();

        // Results for comparison
        TestResult traditional = test("Traditional Pool (1k)", Executors.newFixedThreadPool(1000));

        System.out.println("\n" + " ".repeat(10) + CYAN + "⏳ Clearing memory and preparing for Loom..." + RESET);
        System.gc();
        Thread.sleep(3000);

        TestResult loom = test("Virtual Threads (Loom)", Executors.newVirtualThreadPerTaskExecutor());

        printSummary(traditional, loom);
    }

    static TestResult test(String name, ExecutorService executor) {
        System.out.println("\n" + BOLD + PURPLE + "▶ " + name + RESET);

        Set<String> carrierNames = ConcurrentHashMap.newKeySet();
        LongAdder completed = new LongAdder();

        long startMem = getUsedMemory();
        long startTime = System.currentTimeMillis();

        // Running monitoring in a virtual thread instead of ScheduledExecutorService
        // This eliminates IDE warnings and reduces overhead
        Thread monitorThread = Thread.ofVirtual().start(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    long done = completed.sum();
                    int progress = (int) ((done * 100L) / TASK_COUNT);

                    // Clamp progress to 100 to avoid repeat() overflow
                    int barWidth = Math.min(50, progress / 2);
                    String bar = "█".repeat(barWidth) + "░".repeat(Math.max(0, 50 - barWidth));

                    System.out.printf("\r%s [%s] %d%% | Carriers: %s%d%s",
                            CYAN, bar, progress, YELLOW, carrierNames.size(), CYAN);

                    Thread.sleep(200); // Sleep between updates
                }
            } catch (InterruptedException ignored) {
                // Monitor will be interrupted at the end of the test
            }
        });

        // Main task execution block
        try (executor) {
            // In Java 21+, this automatically handles shutdown() and awaitTermination()
            for (int i = 0; i < TASK_COUNT; i++) {
                executor.submit(() -> {
                    String threadStr = Thread.currentThread().toString();
                    // Extract carrier thread name (ForkJoinPool worker)
                    if (threadStr.contains("@")) {
                        carrierNames.add(threadStr.substring(threadStr.indexOf("@") + 1));
                    }

                    try {
                        Thread.sleep(TASK_DELAY_MS);
                        completed.increment();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        } // Executor closes automatically here, waiting for all tasks to finish

        // Stop monitor thread after task completion
        monitorThread.interrupt();

        long duration = System.currentTimeMillis() - startTime;
        long endMem = getUsedMemory();

        System.out.print("\r" + " ".repeat(120) + "\r"); // Clear line
        System.out.println(GREEN + "✔ Test finished!" + RESET);

        return new TestResult(name, duration, carrierNames.size(), endMem - startMem);
    }

    private static void printHeader() {
        System.out.println(CYAN + BOLD + "=".repeat(60));
        System.out.println("   PROJECT LOOM BENCHMARK");
        System.out.println("   Tasks: " + YELLOW + TASK_COUNT + CYAN + " | Delay: " + YELLOW + TASK_DELAY_MS + "ms");
        System.out.println("   Java:  " + YELLOW + System.getProperty("java.version") + CYAN + " | Cores: " + YELLOW + Runtime.getRuntime().availableProcessors());
        System.out.println("=".repeat(60) + RESET);
    }

    private static void printSummary(TestResult t, TestResult v) {
        System.out.println("\n" + BOLD + "📊 COMPARISON TABLE:" + RESET);
        String format = "| %-25s | %-15s | %-15s |%n";
        String line = "+---------------------------+-----------------+-----------------+";

        System.out.println(line);
        System.out.printf(format, "Metric", "Traditional", "Loom (Virtual)");
        System.out.println(line);
        System.out.printf(format, "Time (sec)", String.format("%.3f", t.duration() / 1000.0), String.format("%.3f", v.duration() / 1000.0));
        System.out.printf(format, "OS Threads (max)", t.carriers(), v.carriers());
        System.out.printf(format, "Memory Delta (MB)", t.memMb(), v.memMb());
        System.out.printf(format, "Memory per task", t.memPerTask(), v.memPerTask());
        System.out.println(line);

        double speedup = (double) t.duration() / v.duration();
        System.out.printf("\n%s🚀 Virtual threads are %s%.1fx%s faster in this scenario!%n",
                BOLD, GREEN, speedup, RESET);
    }

    private static long getUsedMemory() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}