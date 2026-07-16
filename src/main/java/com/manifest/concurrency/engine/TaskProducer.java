package com.manifest.concurrency.engine;

import com.manifest.concurrency.model.PriceUpdateTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Erdem Yusuf
 * Created a TaskProducer class to produce tasks.
 * We can use a TaskQueue to produce tasks but maybe we can add @Component annotation to TaskQueue class.
 */
@Component
public class TaskProducer {

    // we can refactor the parameter list??
    public void produce(List<PriceUpdateTask> tasks, TaskQueue queue, int worker)
            throws InterruptedException {

        for (PriceUpdateTask task : tasks) {
            queue.put(task);
        }

        for (int i = 0; i < worker; i++) {
            queue.put(PriceUpdateTask.POISON_PILL);
        }

    }

}
