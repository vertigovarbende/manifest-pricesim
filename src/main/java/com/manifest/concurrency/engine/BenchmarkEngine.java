package com.manifest.concurrency.engine;

import com.manifest.concurrency.api.request.BenchmarkRequest;
import com.manifest.concurrency.counter.TaskCounter;
import com.manifest.concurrency.counter.impl.SafeTaskCounter;
import com.manifest.concurrency.metrics.ExpectedResultCalculator;
import com.manifest.concurrency.metrics.stats.Benchmark;
import com.manifest.concurrency.metrics.stats.BenchmarkReport;
import com.manifest.concurrency.metrics.stats.RunStats;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.CoinState;
import com.manifest.concurrency.state.SafeCoinState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class BenchmarkEngine {

    private static final List<ThreadMode> THREAD_MODES = List.of(ThreadMode.values());

    private final AtomicReference<BenchmarkReport> latestReport = new AtomicReference<>();
    private final SimulationEngine engine;

    public BenchmarkReport compare(
            BenchmarkRequest request,
            TaskGenerator taskGenerator,
            ExpectedResultCalculator calculator
    ) {
        // Generate tasks
        List<PriceUpdateTask> tasks = taskGenerator.generate(request.updates(), request.seed());

        // Calculate expected results
        Map<String, ExpectedCoinResponse> expected = calculator.calculateExpectedResult(tasks);

        List<Benchmark> results = new ArrayList<>();

        for (ThreadMode threadMode : THREAD_MODES) {
            for (int workers : request.workers()) {

                warmUp(request, tasks, expected, workers, threadMode);
                Benchmark benchmark = measure(request, tasks, expected, workers, threadMode);
                results.add(benchmark);

            }
        }

        BenchmarkReport report = BenchmarkReport.builder()
                .updates(tasks.size())
                .seed(request.seed())
                .warmupRuns(request.warmupRuns())
                .measurementRuns(request.measurementRuns())
                .results(results)
                .build();

        latestReport.set(report);

        return report;
    }

    private void warmUp(
            BenchmarkRequest request,
            List<PriceUpdateTask> tasks,
            Map<String, ExpectedCoinResponse> expected,
            int workers,
            ThreadMode threadMode
    ) {
        for (int i = 0; i < request.warmupRuns(); i++) {
            runOnce(tasks, expected, workers, threadMode);
        }
    }

    private Benchmark measure(
            BenchmarkRequest request,
            List<PriceUpdateTask> tasks,
            Map<String, ExpectedCoinResponse> expected,
            int workers,
            ThreadMode threadMode
    ) {
        List<RunStats> runs = new ArrayList<>();
        for (int i = 0; i < request.measurementRuns(); i++) {
            RunStats runStats = runOnce(tasks, expected, workers, threadMode);
            runs.add(runStats);
        }

        long medianDurationNanos = calculateMedianDuration(runs);

        double elapsedMs = medianDurationNanos / 1_000_000.0;
        double throughput = tasks.size() * 1_000_000_000.0 / medianDurationNanos;

        boolean invariantPassed = runs.stream()
                .allMatch(run -> run.invariant().valid());

        return Benchmark.builder()
                .threadMode(threadMode)
                .updates(tasks.size())
                .workers(workers)
                .elapsedMs(elapsedMs)
                .throughputPerSecond(throughput)
                .invariantPassed(invariantPassed)
                .build();
    }

    private RunStats runOnce(
            List<PriceUpdateTask> tasks,
            Map<String, ExpectedCoinResponse> expected,
            int workers,
            ThreadMode threadMode) {

        CoinState state = new SafeCoinState();
        TaskCounter counter = new SafeTaskCounter();
        TaskQueue queue = new TaskQueue(tasks.size());

        return engine.run("SAFE", threadMode, tasks, workers, expected, state, counter, queue);
    }


    private long calculateMedianDuration(List<RunStats> runs) {
        List<Long> durations = runs.stream()
                .map(RunStats::durationNanos)
                .sorted()
                .toList();

        int middle = durations.size() / 2;

        if (durations.size() % 2 == 1) {
            return durations.get(middle);
        }

        return (durations.get(middle - 1) + durations.get(middle)) / 2;
    }

}
