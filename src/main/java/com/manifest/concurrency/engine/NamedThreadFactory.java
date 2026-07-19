package com.manifest.concurrency.engine;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final ThreadMode threadMode;
    private final AtomicInteger next = new AtomicInteger(1);

    NamedThreadFactory(String prefix, ThreadMode threadMode) {
        this.prefix = prefix;
        this.threadMode = threadMode;
    }

    public Thread newThread(Runnable task) {
        String name = prefix + "-worker-" + next.getAndIncrement();

        if (threadMode == ThreadMode.VIRTUAL) {
            return Thread.ofVirtual().name(name).unstarted(task);
        }

        return Thread.ofPlatform().name(name).unstarted(task);
    }
}
