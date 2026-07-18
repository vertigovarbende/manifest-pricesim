package com.manifest.concurrency.metrics.stats;

import com.manifest.concurrency.metrics.invariant.InvariantReport;
import com.manifest.concurrency.model.CoinSnapshot;
import lombok.Builder;

import java.util.List;

/**
 * @author Erdem Yusuf
 * <p>I created a RunStats class to represent the statistics of a simulation run.</p>
 * <p>We need to add invariant report to see invariant violations !!!!</p>
 */
@Builder
public record RunStats(
        String mode,
        long durationNanos,
        double durationMillis,
        double throughputPerSecond,
        long processedTaskCount,
        List<CoinSnapshot> coins,
        InvariantReport invariant
) {
    // copy the list to make it immutable
    public RunStats {
        coins = List.copyOf(coins);
    }

}
