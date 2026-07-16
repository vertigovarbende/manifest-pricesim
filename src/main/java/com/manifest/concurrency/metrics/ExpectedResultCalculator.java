package com.manifest.concurrency.metrics;

import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import com.manifest.concurrency.model.PriceUpdateTask;
import com.manifest.concurrency.state.CoinCatalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpectedResultCalculator {

    public Map<String, ExpectedCoinResponse> calculateExpectedResult(
            List<PriceUpdateTask> tasks
    ) {
        Map<String, ExpectedCoinResponse> expectedResults = new LinkedHashMap<>();

        CoinCatalog.INITIAL_PRICES.forEach(
                (coinId, initialPrice) ->
                        expectedResults.put(
                                coinId,
                                new ExpectedCoinResponse(
                                        initialPrice,
                                        0
                                )
                        )
        );

        for (PriceUpdateTask task : tasks) {
            if (task.isPoisonPill()) {
                continue;
            }

            ExpectedCoinResponse current =
                    expectedResults.get(task.coinId());

            if (current == null) {
                throw new IllegalArgumentException(
                        "Unknown coin: " + task.coinId()
                );
            }

            ExpectedCoinResponse updated =
                    new ExpectedCoinResponse(
                            current.price() + task.delta(),
                            current.updateCount() + 1
                    );

            expectedResults.put(task.coinId(), updated);
        }

        return Collections.unmodifiableMap(expectedResults);
    }
}

