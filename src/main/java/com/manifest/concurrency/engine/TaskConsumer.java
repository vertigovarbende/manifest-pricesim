package com.manifest.concurrency.engine;

import com.manifest.concurrency.counter.TaskCounter;
import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.CoinState;
import org.springframework.stereotype.Component;


@Component
public final class TaskConsumer {

    public void consume(TaskQueue queue, CoinState state, TaskCounter counter) {
        try {
            while (true) {
                PriceUpdateTask task = queue.take();
                if (task.isPoisonPill()) {
                    return;
                }

                CoinSnapshot snapshot = state.apply(task);
                counter.increment();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
