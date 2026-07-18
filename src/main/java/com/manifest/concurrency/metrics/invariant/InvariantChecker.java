package com.manifest.concurrency.metrics.invariant;

import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.ExpectedCoinResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Erdem Yusuf
 * <p>I created a InvariantChecker class to check the invariants of the simulation.</p>
 * <p>  --- WARN --> task.size() <-- </p>
 */
@Component
public final class InvariantChecker {

    public InvariantReport check(long expectedTaskCount,
                                 long processedTaskCount,
                                 Map<String, ExpectedCoinResponse> expected,
                                 List<CoinSnapshot> actual
    ) {
        List<String> violations = new ArrayList<>();
        boolean processedTasksMatch = expectedTaskCount == processedTaskCount;
        String violationMessage;

        if (!processedTasksMatch) {
            violationMessage = format("Processed" ,"task counter", expectedTaskCount, processedTaskCount);
            violations.add(violationMessage);
        }

        boolean countsMatch = true;
        boolean pricesMatch = true;

        for (CoinSnapshot actualCoin : actual) {
            ExpectedCoinResponse expectedCoin = expected.get(actualCoin.id());

            // check update counts
            if (expectedCoin.updateCount() != actualCoin.updateCount()) {
                countsMatch = false;
                violationMessage = format(actualCoin.id(), "update count", expectedCoin.updateCount(), actualCoin.updateCount());
                violations.add(violationMessage);
            }

            // check prices
            if (expectedCoin.price() != actualCoin.currentPrice()) {
                pricesMatch = false;
                violationMessage = format(actualCoin.id(), "price", expectedCoin.price(), actualCoin.currentPrice());
                violations.add(violationMessage);
            }
        }

        boolean isValid = processedTasksMatch && countsMatch && pricesMatch;

        return InvariantReport.builder()
                .valid(isValid)
                .processedTaskCountMatches(processedTasksMatch)
                .coinUpdateCountsMatch(countsMatch)
                .coinPricesMatch(pricesMatch)
                .violations(violations)
                .build();
    }

    private <T> String format(String comparedValue, String compared, T expected, T actual) {
        return comparedValue + " " + compared + " expected=" + expected + ", actual=" + actual;
    }
}
