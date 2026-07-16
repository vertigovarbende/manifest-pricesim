package com.manifest.concurrency.counter;

/**
 * @author Erdem Yusuf
 */
public interface TaskCounter {

    void increment();

    long value();

}
