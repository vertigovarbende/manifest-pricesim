package com.manifest.concurrency.state;

import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.PriceUpdateTask;

/**
 * @author batuhan
 */

public final class UnsafeCoinState extends AbstractCoinState {

    public CoinSnapshot apply(PriceUpdateTask task) {
        MutableCoin coin = coins.get(task.coinId());
        coin.applyDelta(task.delta());
        return coin.snapshot();
    }

}
