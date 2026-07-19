package com.manifest.concurrency.deadlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public final class DeadlockTransferDemo {

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public DeadlockTransferDemo() {
        locks.put("BTC", new ReentrantLock());
        locks.put("ETH", new ReentrantLock());
        locks.put("SOL", new ReentrantLock());
    }

    public void unsafeTransfer(String fromCoin, String toCoin, CountDownLatch firstLocksTaken)
            throws InterruptedException {
        ReentrantLock firstLock = lockFor(fromCoin);
        ReentrantLock secondLock = lockFor(toCoin);

        firstLock.lockInterruptibly();
        try {
            firstLocksTaken.countDown();
            firstLocksTaken.await();

            secondLock.lockInterruptibly();
            try {
                simulateTransfer(fromCoin, toCoin);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    public void orderedTransfer(String fromCoin, String toCoin) throws InterruptedException {
        String firstCoin = fromCoin.compareTo(toCoin) <= 0 ? fromCoin : toCoin;
        String secondCoin = fromCoin.compareTo(toCoin) <= 0 ? toCoin : fromCoin;

        ReentrantLock firstLock = lockFor(firstCoin);
        ReentrantLock secondLock = lockFor(secondCoin);

        firstLock.lockInterruptibly();
        try {
            secondLock.lockInterruptibly();
            try {
                simulateTransfer(fromCoin, toCoin);
            } finally {
                secondLock.unlock();
            }
        } finally {
            firstLock.unlock();
        }
    }

    private ReentrantLock lockFor(String coinId) {
        ReentrantLock lock = locks.get(coinId);
        if (lock == null) {
            throw new IllegalArgumentException("Unknown coin: " + coinId);
        }
        return lock;
    }

    private void simulateTransfer(String fromCoin, String toCoin) {
        if (fromCoin.equals(toCoin)) {
            throw new IllegalArgumentException("Transfer coins must be different");
        }
    }
}
