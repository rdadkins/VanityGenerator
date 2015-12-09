package com.fatsoapps.vanitygenerator.core.search;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;

/**
 * <b>If this is implemented in Android, you need extend and set the appropriate thread priority.</b><br/>
 *
 * PoolSearch is a Runnable that takes a @BaseSearchListener and searches for Query's defined in a QueryPool. The user
 * can define multiple PoolSearch's in separate threads to achieve a more multi-threaded approach of searching.
 * @see com.fatsoapps.vanitygenerator.core.search.Search for a single threaded instance of searching.
 */
public class PoolSearch implements Runnable {

    private final static ArrayList<BaseSearchListener> listeners = new ArrayList<BaseSearchListener>();
    private GlobalNetParams netParams;
    private QueryPool pool;
    private long updateAmount = 1000;
    private static volatile long startTime = 0;
    private static volatile long generated = 0;

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance. Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     */
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
        this.pool = pool;
        this.netParams = netParams;
        if (startTime == 0) {
            startTime = System.currentTimeMillis();
        }
    }

    public void run() {
        ECKey key;
        RegexQuery query;
        while (!Thread.interrupted() && pool.containsQueries()) {
            key = new ECKey();
            generated++;
            if ((query = pool.matches(key, netParams)) != null) {
                long FINAL_GENERATED = generated;
                if (listeners.size() > 0) {
                    addressFound(key, query.getNetworkParameters(netParams), generated, getGeneratedPerSecond(), query);
                }
                if (!query.isFindUnlimited()) {
                    pool.removeQuery(query);
                    if (!pool.containsQueries()) {
                        if (listeners.size() > 0) {
                            taskCompleted(FINAL_GENERATED, getGeneratedPerSecond());
                        }
                        startTime = 0;
                    }
                }
            }
            if (generated % updateAmount == 0 && listeners.size() > 0) {
                burstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
        listeners.clear();
    }

    public void stop() {
        Thread.currentThread().interrupt();
    }

    private synchronized void addressFound(ECKey key, GlobalNetParams netParams, long generated, long speed, RegexQuery query) {
        for (BaseSearchListener listener: listeners) {
            listener.onAddressFound(key, netParams, generated, speed, query);
        }
    }

    private synchronized void taskCompleted(long generated, long speed) {
        for (BaseSearchListener listener: listeners) {
            listener.onTaskCompleted(generated, speed);
        }
    }

    private synchronized void burstGenerated(long generated, long burstGenerated, long speed) {
        for (BaseSearchListener listener: listeners) {
            listener.updateBurstGenerated(generated, burstGenerated, speed);
        }
    }

    private long getGeneratedPerSecond() {
        try {
            return generated / ((System.currentTimeMillis() - startTime) / 1000);
        } catch (ArithmeticException ex) {
            return generated / 1000;
        }
    }

    public PoolSearch setUpdateAmount(long updateAmount) {
        this.updateAmount = updateAmount;
        return this;
    }

    public PoolSearch registerListener(BaseSearchListener listener) {
        listeners.add(listener);
        return this;
    }

    public PoolSearch unregisterListener(BaseSearchListener listener) {
        listeners.remove(listener);
        return this;
    }

}
