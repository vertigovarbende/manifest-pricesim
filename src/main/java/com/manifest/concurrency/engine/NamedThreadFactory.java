package com.manifest.concurrency.engine;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final AtomicInteger next = new AtomicInteger(1);

    NamedThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    public Thread newThread(Runnable task) {
        return Thread.ofPlatform().name(prefix + "-worker-" + next.getAndIncrement()).unstarted(task);
    }
}
