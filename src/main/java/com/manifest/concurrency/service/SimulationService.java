package com.manifest.concurrency.service;

import com.manifest.concurrency.counter.impl.SafeTaskCounter;
import com.manifest.concurrency.counter.impl.UnsafeTaskCounter;
import com.manifest.concurrency.engine.SimulationEngine;
import com.manifest.concurrency.engine.TaskGenerator;
import com.manifest.concurrency.engine.TaskQueue;
import com.manifest.concurrency.exception.SimulationAlreadyRunningException;
import com.manifest.concurrency.metrics.ExpectedResultCalculator;
import com.manifest.concurrency.metrics.stats.RunStats;
import com.manifest.concurrency.metrics.stats.SimulationResult;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.SafeCoinState;
import com.manifest.concurrency.state.UnsafeCoinState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private final ReentrantLock simulationLock = new ReentrantLock();  // AtomicBoolean??

    public SimulationResult simulate(int updates, int workers, long seed) {
        if (!simulationLock.tryLock()) {
            throw new SimulationAlreadyRunningException("Simulation is already running");
        }

        try {
            // Generate PriceUpdateTasks
            List<PriceUpdateTask> tasks = taskGenerator.generate(updates, seed);

            // Calculate expected results
            // we may use CoinSnapshot instead of ExpectedCoinResponse ???
            Map<String, ExpectedCoinResponse> expected = expectedResultCalculator.calculateExpectedResult(tasks);

            // Create TaskQueue
            TaskQueue queue = new TaskQueue(tasks.size() + workers);

            // Start simulations
            /*
                - We may create Enum for different types of simulations ???
                - i put 'expected' because we gonna use 'expected' for invariant violation report in SIMULATION ENGINE !!!
             */
            RunStats unsafeRun = simulationEngine.run("UNSAFE", tasks, workers, expected, new UnsafeCoinState(), new UnsafeTaskCounter(), queue);
            RunStats safeRun = simulationEngine.run("SAFE", tasks, workers, expected, new SafeCoinState(), new SafeTaskCounter(), queue);

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

            // We need to put lastSafeCoins, lastStats
            return result;

        } finally {
            simulationLock.unlock();
        }

    }


}
