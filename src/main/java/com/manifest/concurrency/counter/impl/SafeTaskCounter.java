package com.manifest.concurrency.counter.impl;

import com.manifest.concurrency.counter.TaskCounter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Erdem Yusuf
 */
public final class SafeTaskCounter implements TaskCounter {

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
