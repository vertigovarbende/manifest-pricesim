package com.manifest.concurrency.engine;

import com.manifest.concurrency.model.PriceUpdateTask;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * @author Erdem Yusuf
 * We can create a Buffer interface and declare put, take and size methods.
 * And we can create BoundedBuffer(ArrayBlockingQueue) and UnboundedBuffer(LinkedBlockingQueue) implementations.
 */
public class TaskQueue {

    private final BlockingQueue<PriceUpdateTask> buffer;

    public TaskQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.buffer = new ArrayBlockingQueue<>(Math.min(capacity, 1000));
    }

    public void put (PriceUpdateTask task) throws InterruptedException {
        buffer.put(task);
    }

    public PriceUpdateTask take() throws InterruptedException {
        return buffer.take();
    }

    public int size() {
        return buffer.size();
    }

}
