package com.manifest.concurrency.model;

public record PriceUpdateTask(
        long sequence,
        String coinId,
        long delta
) {
    public static final PriceUpdateTask POISON_PILL = new PriceUpdateTask(-1, "__STOP__", 0);

    public boolean isPoisonPill() {
        return sequence == -1 && coinId.equals("__STOP__");
    }

}
