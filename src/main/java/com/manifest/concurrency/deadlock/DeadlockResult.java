package com.manifest.concurrency.deadlock;

public record DeadlockResult(
        DeadlockMode mode,
        boolean completed,
        boolean deadlockDetected,
        String message
) {
}
