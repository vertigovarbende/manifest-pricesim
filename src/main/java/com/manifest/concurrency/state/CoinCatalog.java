package com.manifest.concurrency.state;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author batuhan
 */

public final class CoinCatalog {

    public static final Map<String, Long> INITIAL_PRICES;

    static {
        Map<String, Long> prices = new LinkedHashMap<>();
        prices.put("BTC", 60_000L);
        prices.put("ETH", 3_000L);
        prices.put("SOL", 150L);
        INITIAL_PRICES = Collections.unmodifiableMap(prices);
    }

    private CoinCatalog() {

    }
}
