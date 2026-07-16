package com.manifest.concurrency.counter.impl;

import com.manifest.concurrency.counter.TaskCounter;

public class UnsafeTaskCounter implements TaskCounter {

    private long value;

    @Override
    public void increment() {
        value++;
    }

    @Override
    public long value() {
        return value;
    }

}
