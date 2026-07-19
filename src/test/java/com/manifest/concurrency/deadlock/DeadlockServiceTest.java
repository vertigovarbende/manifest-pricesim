package com.manifest.concurrency.deadlock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeadlockServiceTest {

    private final DeadlockService service = new DeadlockService();

    @Test
    void unsafeModeShouldTriggerDeadlockScenario() {
        DeadlockResult result = service.run(DeadlockMode.UNSAFE);

        assertEquals(DeadlockMode.UNSAFE, result.mode());
        assertFalse(result.completed());
        assertTrue(result.deadlockDetected());
    }

    @Test
    void orderedModeShouldCompleteWithoutDeadlock() {
        DeadlockResult result = service.run(DeadlockMode.ORDERED);

        assertEquals(DeadlockMode.ORDERED, result.mode());
        assertTrue(result.completed());
        assertFalse(result.deadlockDetected());
    }
}
