package com.manifest.concurrency.metrics.invariant;

import lombok.Builder;

import java.util.List;

/**
 * @author Erdem Yusuf
 * <p>I created a InvariantReport class to represent the result of an invariant check.</p>
 */
@Builder
public record InvariantReport(
        boolean valid,
        boolean processedTaskCountMatches,
        boolean coinUpdateCountsMatch,
        boolean coinPricesMatch,
        List<String> violations
) {
    // copy the list to make it immutable
    public InvariantReport {
        violations = List.copyOf(violations);
    }
}
