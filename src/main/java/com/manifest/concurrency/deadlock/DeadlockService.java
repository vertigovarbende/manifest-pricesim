package com.manifest.concurrency.deadlock;

import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DeadlockService {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);

    public DeadlockResult run(DeadlockMode mode) {
        if (mode == DeadlockMode.ORDERED) {
            return runOrderedDemo();
        }

        return runUnsafeDemo();
    }

    private DeadlockResult runUnsafeDemo() {
        DeadlockTransferDemo demo = new DeadlockTransferDemo();
        CountDownLatch firstLocksTaken = new CountDownLatch(2);
        ExecutorService executor = Executors.newFixedThreadPool(2, threadFactory("deadlock-demo"));

        try {
            Future<?> first = executor.submit(() -> runInterruptible(() ->
                    demo.unsafeTransfer("BTC", "ETH", firstLocksTaken)));
            Future<?> second = executor.submit(() -> runInterruptible(() ->
                    demo.unsafeTransfer("ETH", "BTC", firstLocksTaken)));

            first.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            second.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            return new DeadlockResult(
                    DeadlockMode.UNSAFE,
                    true,
                    false,
                    "Unsafe demo completed unexpectedly"
            );
        } catch (TimeoutException exception) {
            return new DeadlockResult(
                    DeadlockMode.UNSAFE,
                    false,
                    hasJvmDeadlock(),
                    "Deadlock scenario triggered: BTC and ETH locks were acquired in opposite order"
            );
        } catch (Exception exception) {
            return new DeadlockResult(
                    DeadlockMode.UNSAFE,
                    false,
                    hasJvmDeadlock(),
                    "Unsafe demo failed: " + exception.getMessage()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private DeadlockResult runOrderedDemo() {
        DeadlockTransferDemo demo = new DeadlockTransferDemo();
        ExecutorService executor = Executors.newFixedThreadPool(2, threadFactory("ordered-lock-demo"));

        try {
            Future<?> first = executor.submit(() -> runInterruptible(() ->
                    demo.orderedTransfer("BTC", "ETH")));
            Future<?> second = executor.submit(() -> runInterruptible(() ->
                    demo.orderedTransfer("ETH", "BTC")));

            first.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            second.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            return new DeadlockResult(
                    DeadlockMode.ORDERED,
                    true,
                    false,
                    "Ordered locking completed successfully"
            );
        } catch (Exception exception) {
            return new DeadlockResult(
                    DeadlockMode.ORDERED,
                    false,
                    hasJvmDeadlock(),
                    "Ordered locking failed: " + exception.getMessage()
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean hasJvmDeadlock() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreadIds = threadMXBean.findDeadlockedThreads();
        return deadlockedThreadIds != null && deadlockedThreadIds.length > 0;
    }

    private ThreadFactory threadFactory(String prefix) {
        AtomicInteger counter = new AtomicInteger(1);
        return task -> Thread.ofPlatform()
                .name(prefix + "-" + counter.getAndIncrement())
                .unstarted(task);
    }

    private void runInterruptible(InterruptibleTask task) {
        try {
            task.run();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface InterruptibleTask {
        void run() throws InterruptedException;
    }
}
