package co.bitsquared.vanitygenerator.core.search;

import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;

/**
 * <b>If this is implemented in Android, you need extend and set the appropriate thread priority.</b><br/>
 *
 * PoolSearch is a Runnable that takes a {@code BaseSearchListener} and searches for Query's defined in a {@code QueryPool}. The user
 * can define multiple PoolSearch's in separate threads to achieve a more multi-threaded approach of searching.
 *
 * @see Search for a single threaded instance of searching.
 */
public class PoolSearch implements Runnable {

    private static final int DEFAULT_UPDATE_AMOUNT = 1000;
    private static final ArrayList<BaseSearchListener> listeners = new ArrayList<BaseSearchListener>();
    private GlobalNetParams netParams;
    private QueryPool pool;
    private long updateAmount = DEFAULT_UPDATE_AMOUNT;
    private volatile static long startTime = 0;
    private volatile static long generated = 0;
    private volatile static boolean taskCompleted = true;
    private volatile static boolean burstDoneUpdating = true;
    private volatile static boolean addressDoneUpdating = true;

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance.
     * Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     */
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        registerListener(listener);
        this.pool = pool;
        this.netParams = netParams;
    }

    public void run() {
        synchronized (this) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis() - 1000;
            }
        }
        ECKey key;
        RegexQuery query;
        long localGen;
        while (!Thread.interrupted() && pool.containsQueries()) {
            key = new ECKey();
            localGen = ++generated;
            if ((query = pool.matches(key, netParams)) != null) {
                addressFound(key, query.getNetworkParameters(netParams), generated, getGeneratedPerSecond(), query);
                if (!query.isFindUnlimited()) {
                    pool.removeQuery(query);
                    if (!pool.containsQueries()) {
                        taskCompleted(localGen, getGeneratedPerSecond());
                    }
                }
            }
            if (localGen % updateAmount == 0) {
                burstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
    }

    public void stop() {
        Thread.currentThread().interrupt();
    }

    private void addressFound(final ECKey key, final GlobalNetParams netParams, final long generated, final long speed, final RegexQuery query) {
        synchronized (this) {
            if (!addressDoneUpdating) return;
            addressDoneUpdating = false;
        }
        synchronized (listeners) {
            for (final BaseSearchListener listener: listeners) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onAddressFound(key, netParams, generated, speed, query);
                    }
                }).start();
            }
        }
        addressDoneUpdating = true;
    }

    private void taskCompleted(final long generated, final long speed) {
        synchronized (this) {
            if (!taskCompleted) return;
            taskCompleted = false;
        }
        synchronized (listeners) {
            for (final BaseSearchListener listener: listeners) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onTaskCompleted(generated, speed);
                    }
                }).start();
            }
        }
        taskCompleted = true;
        listeners.clear();
        startTime = 0;
        PoolSearch.generated = 0;
    }

    private void burstGenerated(final long generated, final long burstGenerated, final long speed) {
        synchronized (this) {
            if (!burstDoneUpdating) return;
            burstDoneUpdating = false;
        }
        synchronized (listeners) {
            for (final BaseSearchListener listener: listeners) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listener.updateBurstGenerated(generated, burstGenerated, speed);
                    }
                }).start();
            }
        }
        burstDoneUpdating = true;
    }

    private long getGeneratedPerSecond() {
        return generated / ((System.currentTimeMillis() - startTime) / 1000);
    }

    /**
     * Sets the updating amount when searching.
     * @param updateAmount a positive number interval to update on. If the value is less than 0, the amount will be set to 1000.
     * @return the current instance of PoolSearch
     */
    public PoolSearch setUpdateAmount(long updateAmount) {
        synchronized (this) {
            if (updateAmount <= 0) {
                updateAmount = DEFAULT_UPDATE_AMOUNT;
            }
            this.updateAmount = updateAmount;
        }
        return this;
    }

    /**
     * Registers a BaseSearchListener to this PoolSearch to listen for updates. Updates start as soon as the next update
     * call is implemented.
     */
    public PoolSearch registerListener(BaseSearchListener listener) {
        synchronized (listeners) {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
        return this;
    }

    /**
     * Unregisters a BaseSearchListener from this PoolSearch.
     */
    public PoolSearch unregisterListener(BaseSearchListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
        return this;
    }

}
