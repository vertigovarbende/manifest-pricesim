package com.manifest.concurrency.engine;

import com.manifest.concurrency.model.PriceUpdateTask;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * @author Erdem Yusuf
 * I created a TaskGenerator class to generate tasks.
 * I used a List of coins and a Random object to generate tasks. We may refactor this COINS list
 */
@Component
public class TaskGenerator {

    // refactor!
    private final List<String> COINS = List.of("BTC", "ETH", "SOL");

    public List<PriceUpdateTask> generate(int count, long seed) {

        Random random = new Random(seed);

        List<PriceUpdateTask> tasks = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            long sequence = i + 1L;
            String coinId = COINS.get(random.nextInt(COINS.size()));
            long delta = random.nextLong(-100, 101);

            tasks.add(new PriceUpdateTask(sequence, coinId, delta));
        }
        return tasks;
    }



}
