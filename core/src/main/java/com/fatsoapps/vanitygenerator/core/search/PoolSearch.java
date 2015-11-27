package com.fatsoapps.vanitygenerator.core.search;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import org.bitcoinj.core.ECKey;

import java.util.regex.Matcher;

/**
 * PoolSearch is a Runnable that takes a @BaseSearchListener and searches for Query's defined in a QueryPool. The user
 * can define multiple PoolSearch's in separate threads to achieve a more multi-threaded approach of searching.
 * @see com.fatsoapps.vanitygenerator.core.search.Search for a single threaded instance of searching.
 * <b>If this is implemented in Android, you need to set the appropriate thread priority.</b>
 */
public class PoolSearch implements Runnable {

    private BaseSearchListener listener;
    private GlobalNetParams netParams;
    private QueryPool pool;
    private long updateAmount = 1000;
    private static volatile long startTime = 0;
    private static volatile long generated = 0;
    private static volatile long FINAL_GENERATED;
    private static volatile boolean taskCompletedCalled = false;

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance. Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     */
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        this.listener = listener;
        this.pool = pool;
        this.netParams = netParams;
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
    }

    public void run() {
        ECKey key;
        Matcher matcher;
        while (!Thread.interrupted() && pool.containsQueries()) {
            key = new ECKey();
            generated++;
            if (pool.containsCompressedQueries()) {
                matcher = getMatcher(key, true);
                if (matcher.matches()) {
                    pool.updateQueryList(matcher.group(1), true);
                    if (!pool.containsQueries()) setGeneratedAmount();
                    if (listener != null) listener.onAddressFound(key, generated, getGeneratedPerSecond(), true);
                }
            }
            if (pool.containsUncompressedQueries()) {
                matcher = getMatcher(key, false);
                if (matcher.matches()) {
                    pool.updateQueryList(matcher.group(1), false);
                    if (!pool.containsQueries()) setGeneratedAmount();
                    if (listener != null) listener.onAddressFound(key, generated, getGeneratedPerSecond(), false);
                }
            }
            if (generated % updateAmount == 0 && listener != null) {
                listener.updateBurstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
        if (listener != null && !taskCompletedCalled && FINAL_GENERATED != 0) {
            taskCompletedCalled = true;
            listener.onTaskCompleted(FINAL_GENERATED, getGeneratedPerSecond());
        }
        startTime = 0;
    }

    private long getGeneratedPerSecond() {
        try {
            return generated / ((System.currentTimeMillis() - startTime) / 1000);
        } catch (ArithmeticException ex) {
            return generated / 1000;
        }
    }

    private void setGeneratedAmount() {
        FINAL_GENERATED = generated;
    }

    public PoolSearch setUpdateAmount(long updateAmount) {
        this.updateAmount = updateAmount;
        return this;
    }

    public PoolSearch registerListener(BaseSearchListener listener) {
        this.listener = listener;
        return this;
    }

    public PoolSearch unregisterListener() {
        listener = null;
        return this;
    }

    private Matcher getMatcher(ECKey key, boolean compressed) {
        if (compressed) {
            return pool.getPattern().matcher(key.toAddress(netParams).toString());
        }
        return pool.getUncompressedPattern().matcher(key.decompress().toAddress(netParams).toString());
    }

}
