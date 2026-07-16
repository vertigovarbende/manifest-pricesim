package com.manifest.concurrency.state;

import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.PriceUpdateTask;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author batuhan
 */

public final class SafeCoinState extends AbstractCoinState {

    private final Map<String, ReentrantLock> locks = createLocks();

    public CoinSnapshot apply(PriceUpdateTask task) {
        ReentrantLock lock = locks.get(task.coinId());
        lock.lock();
        try {
            MutableCoin coin = coins.get(task.coinId());
            coin.applyDelta(task.delta());
            return coin.snapshot();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<CoinSnapshot> snapshots() {
        List<CoinSnapshot> result = new ArrayList<>(CoinCatalog.INITIAL_PRICES.size());
        for (String id : CoinCatalog.INITIAL_PRICES.keySet()) {
            ReentrantLock lock = locks.get(id);
            lock.lock();
            try {
                result.add(coins.get(id).snapshot());
            } finally {
                lock.unlock();
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, ReentrantLock> createLocks() {
        Map<String, ReentrantLock> result = new LinkedHashMap<>();
        CoinCatalog.INITIAL_PRICES.keySet().forEach(id -> result.put(id, new ReentrantLock()));
        return Map.copyOf(result);
    }
}
