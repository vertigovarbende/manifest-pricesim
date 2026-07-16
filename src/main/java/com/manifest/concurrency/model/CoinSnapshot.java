package com.manifest.concurrency.model;

/**
 * @author batuhan
 */

public record CoinSnapshot(
        String id,
        long initialPrice,
        long currentPrice,
        long updateCount,
        long lastDelta,
        String lastUpdatedBy
) {

}
