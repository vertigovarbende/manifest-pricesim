package com.manifest.concurrency.engine;

import com.manifest.concurrency.counter.TaskCounter;
import com.manifest.concurrency.exception.SimulationExecutionException;
import com.manifest.concurrency.metrics.invariant.InvariantChecker;
import com.manifest.concurrency.metrics.invariant.InvariantReport;
import com.manifest.concurrency.metrics.stats.RunStats;
import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.CoinState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author Erdem Yusuf
 * <p>I created a SimulationEngine class to run a simulation.</p>
 * <p> WARN ---> please don't put any log in this SimulationEngine class <--- </p>
 */
@Component
@RequiredArgsConstructor
public class SimulationEngine {

    private static final long RUN_TIMEOUT_SECONDS = 120;

    private final TaskProducer producer;
    private final TaskConsumer consumer;
    private final InvariantChecker checker;

    public RunStats run(String simulationMode,
                        ThreadMode threadMode,
                        List<PriceUpdateTask> tasks,
                        int workers,
                        Map<String, ExpectedCoinResponse> expected,
                        CoinState state,
                        TaskCounter counter,
                        TaskQueue queue
    ) {

        ExecutorService executor = createExecutor(workers, simulationMode, threadMode);

        // create futures
        List<Future<?>> futures = new ArrayList<>();

        long started = System.nanoTime();
        boolean completed = false;
        try {
            for (int i = 0; i < workers; i++) {
                futures.add(executor.submit(() -> consumer.consume(queue, state, counter)));
            }
            producer.produce(tasks, queue, workers);
            executor.shutdown();

            waitForWorkers(futures);
            if (!executor.awaitTermination(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            completed = true;
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            if (!completed) {
                executor.shutdownNow();
            }
        }

        List<CoinSnapshot> actual = state.snapshots();

        InvariantReport invariantReport = checker.check(tasks.size(), counter.value(), expected, actual);

        // RunStats variables
        long durationNanos = System.nanoTime() - started;
        long durationMillis = durationNanos / 1_000_000;
        double elapsedMs = durationNanos / 1_000_000.0;
        double throughputPerSecond = counter.value() / (durationNanos / 1_000_000_000.0);

        return RunStats.builder()
                .mode(simulationMode)
                .threadMode(threadMode)
                .durationNanos(durationNanos)
                .durationMillis(durationMillis)
                .elapsedMs(elapsedMs)
                .throughputPerSecond(throughputPerSecond)
                .processedTaskCount(counter.value())
                .coins(actual)
                .invariant(invariantReport)
                .build();

    }

    private ExecutorService createExecutor(int workers, String simulationMode, ThreadMode threadMode) {
        ThreadFactory threadFactory = new NamedThreadFactory(simulationMode, threadMode);

        if (threadMode == ThreadMode.VIRTUAL) {
            return Executors.newThreadPerTaskExecutor(threadFactory);
        }

        return Executors.newFixedThreadPool(workers, threadFactory);
    }

    private void waitForWorkers(List<Future<?>> workerFutures) throws InterruptedException {

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(RUN_TIMEOUT_SECONDS);
        for (Future<?> future : workerFutures) {
            long remaining = deadline - System.nanoTime();

            if (remaining <= 0) {
                throw new SimulationExecutionException("Workers did not finish in time");
            }

            try {
                future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (ExecutionException exception) {
                throw new SimulationExecutionException("A worker failed", exception.getCause());
            } catch (TimeoutException exception) {
                throw new SimulationExecutionException("Workers did not finish in time", exception);
            }
        }
    }

    // Graceful shutdown - we can put this method into run() method
    private void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(RUN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}
