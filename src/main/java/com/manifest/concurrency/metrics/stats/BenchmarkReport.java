package com.manifest.concurrency.metrics.stats;


import lombok.Builder;

import java.util.List;

/**
 * @author Erdem Yusuf
 */
@Builder
public record BenchmarkReport(
        int updates,
        long seed,
        int warmupRuns,
        int measurementRuns,
        List<Benchmark> results
) {
    public BenchmarkReport {
        results = List.copyOf(results);
    }
}
