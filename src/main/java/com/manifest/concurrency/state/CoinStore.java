package com.manifest.concurrency.state;


import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.model.PriceUpdateTask;

import java.util.List;

/**
 * @author batuhan
 */

public interface CoinStore {

    CoinSnapshot apply(PriceUpdateTask task);

    List<CoinSnapshot> snapshots();

}
