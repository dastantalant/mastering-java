package com.mastering.loom;

public record TestResult(String name, long duration, int carriers, long memBytes) {
    String memMb() {
        return String.format("%.2f", memBytes / 1024.0 / 1024.0);
    }

    String memPerTask() {
        double bytes = (double) memBytes / LoomDemo.TASK_COUNT;
        if (Math.abs(bytes) < 1024) return String.format("%.0f B", bytes);
        return String.format("%.1f KB", bytes / 1024.0);
    }
}