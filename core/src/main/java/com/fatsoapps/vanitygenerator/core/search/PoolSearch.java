package com.fatsoapps.vanitygenerator.core.search;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;

/**
 * <b>If this is implemented in Android, you need extend and set the appropriate thread priority.</b><br/>
 *
 * PoolSearch is a Runnable that takes a {@code BaseSearchListener} and searches for Query's defined in a {@code QueryPool}. The user
 * can define multiple PoolSearch's in separate threads to achieve a more multi-threaded approach of searching.
 *
 * @see com.fatsoapps.vanitygenerator.core.search.Search for a single threaded instance of searching.
 */
public class PoolSearch implements Runnable {

    private static volatile ArrayList<BaseSearchListener> listeners = new ArrayList<BaseSearchListener>();
    private GlobalNetParams netParams;
    private QueryPool pool;
    private long updateAmount = 5000;
    private static volatile long startTime = 0;
    private static volatile long generated = 0;
    private static boolean taskCompleted = true;
    private static boolean burstDoneUpdating = true;

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance.
     * Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     */
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        if (!listeners.contains(listener)) {
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
                    }
                }
            }
            if (generated % updateAmount == 0 && listeners.size() > 0) {
                burstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
    }

    public void stop() {
        Thread.currentThread().interrupt();
    }

    private synchronized void addressFound(final ECKey key, final GlobalNetParams netParams, final long generated, final long speed, final RegexQuery query) {
        new Thread(new Runnable() {
            public void run() {
                for (BaseSearchListener listener: listeners) {
                    listener.onAddressFound(key, netParams, generated, speed, query);
                }
            }
        }).run();
    }

    private synchronized void taskCompleted(final long generated, final long speed) {
        if (!taskCompleted) return;
        taskCompleted = false;
        new Thread(new Runnable() {
            public void run() {
                for (BaseSearchListener listener: listeners) {
                    listener.onTaskCompleted(generated, speed);
                }
            }
        }).run();
        taskCompleted = true;
        listeners.clear();
        startTime = 0;
        PoolSearch.generated = 0;
    }

    private synchronized void burstGenerated(final long generated, final long burstGenerated, final long speed) {
        if (!burstDoneUpdating) return;
        burstDoneUpdating = false;
        new Thread(new Runnable() {
            public void run() {
                for (final BaseSearchListener listener: listeners) {
                    listener.updateBurstGenerated(generated, burstGenerated, speed);
                }
            }
        }).run();
        burstDoneUpdating = true;

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
