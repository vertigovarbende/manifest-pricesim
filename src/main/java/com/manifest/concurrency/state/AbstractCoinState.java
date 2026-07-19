package com.manifest.concurrency.state;


import com.manifest.concurrency.model.CoinSnapshot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author batuhan
 */

abstract class AbstractCoinState implements CoinState {
    protected final Map<String, MutableCoin> coins = new LinkedHashMap<>();

    AbstractCoinState() {
        CoinCatalog.INITIAL_PRICES.forEach((id, price) ->
                coins.put(id, new MutableCoin(id, price))
        );
    }

    public List<CoinSnapshot> snapshots() {

        return coins.values().stream().map(MutableCoin::snapshot).toList();
    }
}
