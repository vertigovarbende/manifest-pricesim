package com.manifest.concurrency.engine;

import java.util.concurrent.ThreadFactory;

final class NamedThreadFactory implements ThreadFactory {

    private final ThreadFactory delegate;

    private NamedThreadFactory(ThreadFactory delegate) {
        this.delegate = delegate;
    }

    static ThreadFactory platform(String prefix) {
        return new NamedThreadFactory(Thread.ofPlatform()
                .name(workerPrefix(prefix), 1)
                .factory());
    }

    static ThreadFactory virtual(String prefix) {
        return new NamedThreadFactory(Thread.ofVirtual()
                .name(workerPrefix(prefix), 1)
                .factory());
    }

    public Thread newThread(Runnable task) {
        return delegate.newThread(task);
    }

    private static String workerPrefix(String prefix) {
        return prefix + "-worker-";
    }
}
