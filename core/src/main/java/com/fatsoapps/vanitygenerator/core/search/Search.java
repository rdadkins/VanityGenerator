package com.fatsoapps.vanitygenerator.core.search;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Search is a Runnable that takes a @BaseSearchListener and searches for RegexQuery's (or Query's) defined by a user.
 * Updates occur by definitions in place of BaseSearchListener. Each Search has its own list of queries that it is
 * searching against.
 * @see com.fatsoapps.vanitygenerator.core.search.BaseSearchListener for usage.
 * @see com.fatsoapps.vanitygenerator.core.search.PoolSearch for a more multi-threaded approach.
 * <b>If this is implemented on Android, you need to extend this class and set the appropriate thread priority.</b>
 */
public class Search implements Runnable {

    protected BaseSearchListener listener;
    private GlobalNetParams netParams;
    protected ArrayList<? extends RegexQuery> queries;
    private long updateAmount = 1000;
    private long generated;
    private long startTime;

    public <T extends RegexQuery> Search(BaseSearchListener listener, GlobalNetParams netParams, final T... queries) {
        this.listener = listener;
        this.netParams = netParams;
        this.queries = new ArrayList<RegexQuery>(Arrays.asList(queries));
        startTime = System.currentTimeMillis();
    }

    public Search setUpdateAmount(long amount) {
        updateAmount = amount;
        return this;
    }

    public void run() {
        ECKey key;
        while (!Thread.interrupted() && queries.size() > 0) {
            key = new ECKey();
            generated++;
            for (RegexQuery query: queries) {
                if (query.matches(key, netParams)) {
                    if (listener != null) {
                        listener.onAddressFound(key, query.getNetworkParameters(netParams), generated, getGeneratedPerSecond(), query);
                    }
                    if (!query.isFindUnlimited()) {
                        queries.remove(query);
                        break;
                    }
                }
            }
            if (generated % updateAmount == 0 && listener != null) {
                listener.updateBurstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
        if (listener != null) {
            listener.onTaskCompleted(generated, getGeneratedPerSecond());
        }
    }

    private long getGeneratedPerSecond() {
        try {
            return generated / ((System.currentTimeMillis() - startTime) / 1000);
        } catch (ArithmeticException ex) {
            return generated / 1000;
        }
    }

}
