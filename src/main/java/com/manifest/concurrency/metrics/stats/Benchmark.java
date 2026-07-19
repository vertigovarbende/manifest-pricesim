package com.manifest.concurrency.metrics.stats;

import com.manifest.concurrency.engine.ThreadMode;
import lombok.Builder;

@Builder
public record Benchmark(
        ThreadMode threadMode,
        long updates,
        int workers,
        double elapsedMs,
        double throughputPerSecond,
        boolean invariantPassed
) {
}
