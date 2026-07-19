package com.manifest.concurrency.api.request;

import com.manifest.concurrency.engine.ThreadMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;

import java.util.List;

@Builder
public record BenchmarkRequest(
        @Min(1)
        @Max(100_000)
        int updates,

        List<@Min(1) @Max(8) Integer> workers,

        long seed,

        @Min(0)
        @Max(10)
        int warmupRuns,

        @Min(1)
        @Max(20)
        int measurementRuns
) {

}
