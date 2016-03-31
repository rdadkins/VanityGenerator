package co.bitsquared.vanitygenerator.core.search;

import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Search is a Runnable that takes a @BaseSearchListener and searches for RegexQuery's (or Query's) defined by a user.
 * Updates occur by definitions in place of BaseSearchListener. Each Search has its own list of queries that it is
 * searching against.
 * @see BaseSearchListener for usage.
 * @see PoolSearch for a more multi-threaded approach.
 * <b>If this is implemented on Android, you need to extend this class and set the appropriate thread priority.</b> * @deprecated Please use PoolSearch as Search is no longer supported.
 * @deprecated Please use PoolSearch as Search is no longer supported.
 * @since v1.2.0
 */
@Deprecated
public class Search implements Runnable {

    private static final int DEFAULT_UPDATE_AMOUNT = 1000;

    private BaseSearchListener listener;
    private GlobalNetParams netParams;
    private ArrayList<RegexQuery> queries;
    private long updateAmount = DEFAULT_UPDATE_AMOUNT;
    private long generated;
    private long startTime;

    public Search(BaseSearchListener listener, GlobalNetParams netParams, final RegexQuery... queries) {
        this.listener = listener;
        this.netParams = netParams;
        this.queries = new ArrayList<RegexQuery>(Arrays.asList(queries));
    }

    /**
     * Sets the updating amount when searching.
     * @param amount a positive number interval to update on. If the value is less than 0, the default will be set to 1000.
     * @return the current instance of Search
     */
    public Search setUpdateAmount(long amount) {
        if (amount <= 0) {
            amount = DEFAULT_UPDATE_AMOUNT;
        }
        updateAmount = amount;
        return this;
    }

    public void run() {
        startTime = System.currentTimeMillis() - 1000;
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

    public void stop() {
        Thread.currentThread().interrupt();
    }

    private long getGeneratedPerSecond() {
        return generated / ((System.currentTimeMillis() - startTime) / 1000);
    }

}
