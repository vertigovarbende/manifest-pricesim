package com.manifest.concurrency.counter.impl;

import com.manifest.concurrency.counter.TaskCounter;

/**
 * @author Erdem Yusuf
 */
import java.util.concurrent.atomic.AtomicLong;

public class SafeTaskCounter implements TaskCounter {

    private final AtomicLong value = new AtomicLong();

    @Override
    public void increment() {
        value.incrementAndGet();
    }

    @Override
    public long value() {
        return value.get();
    }

}
