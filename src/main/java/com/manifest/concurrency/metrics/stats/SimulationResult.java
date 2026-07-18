package com.manifest.concurrency.metrics.stats;

import com.manifest.concurrency.model.ExpectedCoinResponse;
import lombok.Builder;

import java.util.Map;

/**
 * @author Erdem Yusuf
 * <p>I created a SimulationResult class to represent the result of a simulation.</p>
 * <p> WE CAN USE CoinSnapshot to represent 'expected' I GUESS</p>
 */
@Builder
public record SimulationResult(
        int submittedUpdates,
        int workers,
        long seed,
        int generatedTaskCount,
        Map<String, ExpectedCoinResponse> expected,
        RunStats unsafeRun,
        RunStats safeRun,
        RunStats safeVirtualRun
) {

    // copy the map to make it immutable
    public SimulationResult {
        expected = Map.copyOf(expected);
    }

}
