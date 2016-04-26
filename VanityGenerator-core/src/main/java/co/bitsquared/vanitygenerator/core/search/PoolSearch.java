package co.bitsquared.vanitygenerator.core.search;

import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.listeners.QueryPoolListener;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.tools.Utils;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;

/**
 * PoolSearch is a Runnable that takes a {@code BaseSearchListener} and searches for Query's defined in a {@code QueryPool}. The user
 * can define multiple PoolSearch's in separate threads to achieve a more multi-threaded approach of searching.
 *
 * Android implementation can be found in the android module.
 */
public class PoolSearch implements Runnable, QueryPoolListener {

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
    private static final ArrayList<PoolSearch> poolSearchReferences = new ArrayList<PoolSearch>();
    private SearchMode searchMode;
    private boolean isSearching = true;
    private boolean forceStop = false;

    /**
     * Creates a PoolSearch thread from a listener, an existing QueryPool instance, and an existing GlobalNetParams
     * instance.
     * Note: if the reference to this GNP changes, it WILL affect the way this thread searches for Query's.
     * @deprecated use PoolSearchBuilder. Deprecated since v1.3.0
     * @since v1.0.0
     */
    @Deprecated
    public PoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        registerListener(listener);
        synchronized (this) {
            if (this.pool == null) {
                this.pool = pool;
            }
            if (this.netParams == null) {
                this.netParams = netParams;
            }
            searchMode = SearchMode.SEARCH_ALL;
        }
    }

    /**
     * Creates a PoolSearch from a PoolSearchBuilder
     * @since v1.3.0
     */
    protected PoolSearch(PoolSearchBuilder builder) {
        registerListener(builder.listener);
        pool = builder.pool;
        netParams = builder.netParams;
        searchMode = builder.searchMode;
        poolSearchReferences.add(this);
    }

    @Override
    public void run() {
        pool.registerListener(this);
        setStartTimeToNow();
        if (searchMode == SearchMode.SEARCH_ALL) {
            searchAll();
        } else if (searchMode == SearchMode.EASIEST_HARDEST || searchMode == SearchMode.HARDEST_EASIEST) {
            searchInOrder();
        }
        pool.unregisterListener(this);
        if (!pool.containsQueries()) {
            taskCompleted(generated, getGeneratedPerSecond());
        }
    }

    @Override
    public void onQueryRemoved(RegexQuery query) {
        boolean forceStopAll = !pool.containsQueries();
        synchronized (poolSearchReferences) {
            for (PoolSearch search: poolSearchReferences) {
                search.isSearching = false;
                search.forceStop = forceStopAll;
            }
        }
    }

    @Override
    public void onQueryAdded(RegexQuery query) {
        synchronized (poolSearchReferences){
            for (PoolSearch search: poolSearchReferences) {
                search.isSearching = false;
            }
        }
    }

    private void searchAll() {
        ECKey key;
        RegexQuery query;
        long localGen = 0;
        while (!forceStop) {
            key = new ECKey();
            localGen = ++generated;
            if ((query = pool.matches(key, netParams)) != null) {
                addressFound(key, query.getNetworkParameters(netParams), generated, getGeneratedPerSecond(), query);
                if (!query.isFindUnlimited()) {
                    pool.removeQuery(query);
                }
            }
            if (canBurstUpdate(localGen)) {
                burstGenerated(generated, updateAmount, getGeneratedPerSecond());
            }
        }
    }

    private void searchInOrder() {
        System.out.println("SIO " + Thread.currentThread().getId());
        RegexQuery query = getNextQuery();
        if (query == null) {
            return;
        }
        ECKey key;
        long localGen;
        isSearching = true;
        while (isSearching) {
            key = new ECKey();
            localGen = ++generated;
            if (query.matches(key, netParams)) {
                addressFound(key, netParams, localGen, getGeneratedPerSecond(), query);
                pool.removeQuery(query);
                break;
            }
            if (canBurstUpdate(localGen)) {
                burstGenerated(localGen, updateAmount, getGeneratedPerSecond());
            }
        }
        if (!forceStop) {
            searchInOrder();
        }
    }

    private boolean canBurstUpdate(long generated) {
        return generated % updateAmount == 0;
    }

    private RegexQuery getNextQuery() {
        RegexQuery query;
        if (searchMode == SearchMode.EASIEST_HARDEST) {
            query = pool.getEasiestQuery();
        } else if (searchMode == SearchMode.HARDEST_EASIEST) {
            query = pool.getHardestQuery();
        } else {
            query = pool.getEasiestQuery();
        }
        return query;
    }

    public void stop() {
        forceStop = true;
        isSearching = false;
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
        if (listener != null) {
            synchronized (listeners) {
                if (!listeners.contains(listener)) {
                    listeners.add(listener);
                }
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

    private long getGeneratedPerSecond() {
        return generated / ((System.currentTimeMillis() - startTime) / 1000);
    }

    private void setStartTimeToNow() {
        synchronized (this) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis() - 1000;
            }
        }
    }

    /**
     * PoolSearchBuilder is a builder for PoolSearch that requires a QueryPool. A BaseSearchListener, GlobalNetParams, and SearchMode
     * are all optional.
     */
    public static class PoolSearchBuilder {

        private BaseSearchListener listener;
        private QueryPool pool;
        private SearchMode searchMode;
        private GlobalNetParams netParams;

        /**
         * Create a PoolSearchBuilder from a QueryPool.
         * @param pool a nonnull QueryPool.
         * @throws NullPointerException if pool is null.
         */
        public PoolSearchBuilder(QueryPool pool) {
            Utils.checkNotNull(pool, "QueryPool cannot be null.");
            this.pool = pool;
        }

        /**
         * Sets a BaseSearchListener to be used for updating while searching. This doesn't need to be registered right away.
         */
        public PoolSearchBuilder searchListener(BaseSearchListener listener) {
            this.listener = listener;
            return this;
        }

        /**
         * Sets the default network parameters to be used if queries in the QueryPool do not define their own network.
         *
         * @throws NullPointerException if the network provided is null
         */
        public PoolSearchBuilder netParams(GlobalNetParams netParameters) {
            Utils.checkNotNull(netParameters, "GlobalNetParams cannot be null.");
            netParams = netParameters;
            return this;
        }

        /**
         * SearchMode is independent of all other PoolSearch implementations meaning that one PoolSearch can have a different
         * searching mode than another PoolSearch. Default is set to SEARCH_ALL
         *
         * @see co.bitsquared.vanitygenerator.core.search.SearchMode
         */
        public PoolSearchBuilder searchMode(SearchMode searchMode) {
            this.searchMode = searchMode;
            return this;
        }

        public PoolSearch build() {
            if (netParams == null) {
                netParams = pool.getNetwork().toGlobalNetParams();
            }
            if (searchMode == null) {
                searchMode = SearchMode.SEARCH_ALL;
            }
            return new PoolSearch(this);
        }

    }

}
