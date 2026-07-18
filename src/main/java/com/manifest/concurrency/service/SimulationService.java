package com.manifest.concurrency.service;

import com.manifest.concurrency.counter.impl.SafeTaskCounter;
import com.manifest.concurrency.counter.impl.UnsafeTaskCounter;
import com.manifest.concurrency.engine.SimulationEngine;
import com.manifest.concurrency.engine.TaskGenerator;
import com.manifest.concurrency.engine.TaskQueue;
import com.manifest.concurrency.engine.ThreadMode;
import com.manifest.concurrency.exception.SimulationAlreadyRunningException;
import com.manifest.concurrency.exception.SimulationNotFoundException;
import com.manifest.concurrency.metrics.ExpectedResultCalculator;
import com.manifest.concurrency.metrics.stats.Benchmark;
import com.manifest.concurrency.metrics.stats.RunStats;
import com.manifest.concurrency.metrics.stats.SimulationResult;
import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.SafeCoinState;
import com.manifest.concurrency.state.UnsafeCoinState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Erdem Yusuf
 * <p> I created this class to simulate the coin state and task counter using different strategies. </p>
 */
@Service
@RequiredArgsConstructor
public class SimulationService {

    private final ExpectedResultCalculator expectedResultCalculator;
    private final TaskGenerator taskGenerator;
    private final SimulationEngine simulationEngine;
    private final ReentrantLock simulationLock = new ReentrantLock();// AtomicBoolean??
    private final AtomicReference<SimulationResult> latestStats = new AtomicReference<>();
    private final AtomicReference<List<CoinSnapshot>> latestCoin = new AtomicReference<>();
    private final List<Benchmark> benchmarks = new ArrayList<>();

    public SimulationResult simulate(int updates, int workers, long seed, ThreadMode threadMode) {
        if (!simulationLock.tryLock()) {
            throw new SimulationAlreadyRunningException("Simulation is already running");
        }

        try {
            ThreadMode selectedThreadMode;
            if (threadMode == null) {
                selectedThreadMode = ThreadMode.PLATFORM;
            } else {
                selectedThreadMode = threadMode;
            }

            // Generate PriceUpdateTasks
            List<PriceUpdateTask> tasks = taskGenerator.generate(updates, seed);

            // Calculate expected results
            Map<String, ExpectedCoinResponse> expected = expectedResultCalculator.calculateExpectedResult(tasks);

            // Create TaskQueues
            TaskQueue unsafeQueue = new TaskQueue(tasks.size() + workers);
            TaskQueue safeQueue = new TaskQueue(tasks.size() + workers);

            // Start simulations
            RunStats unsafeRun = simulationEngine.run("UNSAFE", selectedThreadMode, tasks, workers, expected, new UnsafeCoinState(), new UnsafeTaskCounter(), unsafeQueue);
            RunStats safeRun = simulationEngine.run("SAFE", selectedThreadMode, tasks, workers, expected, new SafeCoinState(), new SafeTaskCounter(), safeQueue);

            // Create SimulationResult
            SimulationResult result = SimulationResult.builder()
                    .submittedUpdates(updates)
                    .workers(workers)
                    .seed(seed)
                    .generatedTaskCount(tasks.size())
                    .expected(expected)
                    .unsafeRun(unsafeRun)
                    .safeRun(safeRun)
                    .build();

            latestStats.set(result);
            latestCoin.set(result.safeRun().coins());

            // Create Benchmark
            Benchmark benchmark = Benchmark.builder()
                    .threadMode(selectedThreadMode)
                    .updates(safeRun.totalUpdateCount())
                    .workers(workers)
                    .elapsedMs(safeRun.elapsedMs())
                    .throughputPerSecond(safeRun.throughputPerSecond())
                    .invariantPassed(safeRun.invariant().valid())
                    .build();
            benchmarks.add(benchmark);

            return result;
        } finally {
            simulationLock.unlock();
        }

    }

    public List<CoinSnapshot> coins() {
        List<CoinSnapshot> latestCoin = this.latestCoin.get();
        if (latestCoin == null)
            throw new SimulationNotFoundException("Simulation not found");
        return latestCoin;
    }

    public SimulationResult stats() {
        SimulationResult result = this.latestStats.get();
        if (result == null)
            throw new SimulationNotFoundException("Simulation not found");
        return result;
    }

    public List<Benchmark> benchmarks() {
        if (benchmarks.isEmpty()) {
            throw new SimulationNotFoundException("Simulation not found");
        }
        return List.copyOf(benchmarks);
    }
}

