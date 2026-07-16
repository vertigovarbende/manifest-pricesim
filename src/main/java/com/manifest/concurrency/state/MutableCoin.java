package com.manifest.concurrency.state;

import com.manifest.concurrency.model.CoinSnapshot;

/**
 * @author batuhan
 */

final class MutableCoin {

    final String id;
    final long initialPrice;
    long currentPrice;
    long updateCount;
    long lastDelta;
    String lastUpdatedBy;

    MutableCoin(String id, long price) {
        this.id = id;
        initialPrice = price;
        currentPrice = price;
    }

    void applyDelta(long delta) {
        currentPrice += delta;
        updateCount++;
        lastDelta = delta;
        lastUpdatedBy = Thread.currentThread().getName();
    }

    CoinSnapshot snapshot() {
        return new CoinSnapshot(id, initialPrice, currentPrice, updateCount, lastDelta, lastUpdatedBy);
    }
}
