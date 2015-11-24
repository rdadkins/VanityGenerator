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

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance. Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     */
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        this.listener = listener;
        this.pool = pool;
        this.netParams = netParams;
    }

    public void run() {
        ECKey key = new ECKey();
        Matcher matcher;
        long generated = 1;
        while (!Thread.interrupted() && pool.containsQueries()) {
            if (pool.containsCompressedQueries()) {
                matcher = getMatcher(key, true);
                if (matcher.matches()) {
                    pool.updateQueryList(matcher.group(1));
                    if (listener != null) listener.onAddressFound(key, true);
                }
            }
            if (pool.containsUncompressedQueries()) {
                matcher = getMatcher(key, false);
                if (matcher.matches()) {
                    pool.updateQueryList(matcher.group(1));
                    if (listener != null) listener.onAddressFound(key, false);
                }
            }
            if (generated % updateAmount == 0 && listener != null) {
                listener.updateBurstGenerated(generated, updateAmount);
            }
            key = new ECKey();
            generated++;
        }
        if (listener != null) listener.onTaskCompleted(generated);
    }

    public void setUpdateAmount(long updateAmount) {
        this.updateAmount = updateAmount;
    }

    public void registerListener(BaseSearchListener listener) {
        this.listener = listener;
    }

    public void unregisterListener() {
        listener = null;
    }

    private Matcher getMatcher(ECKey key, boolean compressed) {
        if (compressed) {
            return pool.getPattern().matcher(key.toAddress(netParams).toString());
        }
        return pool.getUncompressedPattern().matcher(key.toAddress(netParams).toString());
    }

}
