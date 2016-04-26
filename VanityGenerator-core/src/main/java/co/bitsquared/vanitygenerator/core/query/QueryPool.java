package co.bitsquared.vanitygenerator.core.query;

import co.bitsquared.vanitygenerator.core.exceptions.IllegalDecimalVersionException;
import co.bitsquared.vanitygenerator.core.listeners.QueryPoolListener;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.network.Network;
import co.bitsquared.vanitygenerator.core.search.PoolSearch;
import org.bitcoinj.core.ECKey;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.TreeSet;

/**
 * QueryPool is collection of Query's defined by the user that can be accessed anywhere by calling getInstance().
 * General usage is to create threads that rely on the Query's within QueryPool and to add / update / delete
 * accordingly. RegexQuery's are not included in this class since each RQ can contain multiple sub-queries which cannot
 * be easily handled and removed when a match is found.
 * @see PoolSearch to see usage of QueryPool.
 */
public class QueryPool {

    private Network network;
    private final TreeSet<RegexQuery> queries;
    private GlobalNetParams netParams;
    private final ArrayList<QueryPoolListener> listeners = new ArrayList<QueryPoolListener>();

    private static QueryPool instance;

    /**
     * Tries to return the instance of QueryPool. If it doesn't exist, an exception is thrown.
     * @return instance of QueryPool.
     */
    @Nullable
    public static synchronized QueryPool getInstance() {
        return instance;
    }

    /**
     * Creates an instance of QueryPool with a starting Network.
     * @param network - The network to create the QueryPool instance.
     * @param updateNetworkIfExists - Updates the current network if an instance already exists.
     * @return the single instance of QueryPool.
     */
    public static synchronized QueryPool getInstance(Network network, boolean updateNetworkIfExists) {
        if (instance == null) {
            instance = new QueryPool(network);
        } else if (updateNetworkIfExists && instance.network != network) {
            instance.updateNetwork(network);
        }
        return instance;
    }

    private QueryPool(Network network) {
        queries = new TreeSet<RegexQuery>();
        netParams = new GlobalNetParams(network);
        this.network = network;
    }

    private QueryPool(int publicKeyHeader, int p2shHeader, int privateKeyHeader) throws IllegalDecimalVersionException {
        queries = new TreeSet<RegexQuery>();
        netParams = new GlobalNetParams(publicKeyHeader, p2shHeader, privateKeyHeader);
    }

    public synchronized <T extends RegexQuery> void addQuery(T query) {
        synchronized (queries) {
            if (queries.contains(query)) return;
            queries.add(query);
            updateListenersAdded(query);
        }
    }

    /**
     * Removes a query based off of its original hashcode.
     * @since v1.0.0
     */
    public synchronized void removeQuery(int originalHashCode) {
        synchronized (queries) {
            RegexQuery queryToBeRemoved = null;
            for (RegexQuery query: queries) {
                if (query.hashCode() == originalHashCode) {
                    queryToBeRemoved = query;
                    break;
                }
            }
            removeQuery(queryToBeRemoved);
        }
    }

    /**
     * Removes a query from the pool.
     * @since v1.0.0
     */
    public synchronized void removeQuery(RegexQuery query) {
        if (query == null) return;
        synchronized (queries) {
            if (queries.remove(query)) {
                updateListenersRemoved(query);
            }
        }
    }

    /**
     * Updates an old query based on the original hashcode.
     * @since v1.0.0
     */
    public synchronized void updateQuery(RegexQuery newQuery, int originalHashCode) {
        if (newQuery == null || newQuery.hashCode() == originalHashCode || contains(newQuery)) return;
        synchronized (queries) {
            RegexQuery queryToRemove = null;
            for (RegexQuery query: queries) {
                if (query.hashCode() == originalHashCode) {
                    queryToRemove = query;
                    break;
                }
            }
            if (queryToRemove == null) return;
            queries.remove(queryToRemove);
            queries.add(newQuery);
        }
    }

    /**
     * Checks to see if the pool contains this query.
     * @since v1.0.0
     */
    public boolean contains(RegexQuery query) {
        synchronized (queries) {
            return query != null && queries.contains(query);
        }
    }

    /**
     * Returns the amount of queries remaining in the pool.
     * @since v1.0.0
     */
    public int getAmountOfQueries() {
        synchronized (queries) {
            return queries.size();
        }
    }

    /**
     * Determines if there are queries present in the pool.
     * @since v1.0.0
     */
    public boolean containsQueries() {
        return getAmountOfQueries() > 0;
    }

    /**
     * When a user decides to change the Network they are searching on, all of the queries need to be updated with
     * the new updated Network
     * @param network - Network that is going to replace the old Network.
     * @since v1.0.0
     */
    public void updateNetwork(Network network) {
        if (this.network != network) {
            this.network = network;
            synchronized (queries) {
                for (RegexQuery query: queries) {
                    query.updateNetwork(network);
                }
            }
        }
    }

    /**
     * When a user decides to change the GlobalNetParams they are searching on, all of the queries need to be updated
     * with the new GNP. This method is used over updateNetwork(Network) when the networks module is excluded.
     * @param netParams - GNP that is going to replace the old GNP.
     * @deprecated since v1.3.0. Just use updateNetwork(Network)
     */
    public void updateNetwork(GlobalNetParams netParams) {
        if (this.netParams.getAddressHeader() != netParams.getAddressHeader()) {
            this.netParams = netParams;
            synchronized (queries) {
                for (RegexQuery query: queries) {
                    if (query instanceof NetworkQuery) {
                        query.updateNetParams(netParams);
                    }
                }
            }
        }
    }

    /**
     * Returns the specified network to search on.
     * @since v1.0.0
     * @deprecated since v1.3.0
     */
    @Deprecated
    public Network getNetwork() {
        return network;
    }

    /**
     * Determines if a an ECKey matches any query in the pool. If so, the matched query is returned. Otherwise, null is returned.
     * @since v1.0.0
     */
    public RegexQuery matches(ECKey key, GlobalNetParams netParams) {
        synchronized (queries) {
            for (RegexQuery query: queries) {
                if (query.matches(key, netParams)) {
                    return query;
                }
            }
        }
        return null;
    }

    /**
     * Returns the easiest / lowest ranking query if there is one. Otherwise, returns null.
     * @since v1.3.0
     */
    public RegexQuery getEasiestQuery() {
        synchronized (queries) {
            if (queries.isEmpty()) {
                return null;
            }
            return queries.first();
        }
    }

    /**
     * Returns the hardest / highest ranking query if there is one. Otherwise, returns null.
     * @since v1.3.0
     */
    public RegexQuery getHardestQuery() {
        synchronized (queries) {
            if (queries.isEmpty()) {
                return null;
            }
            return queries.last();
        }
    }

    public void registerListener(QueryPoolListener listener) {
        synchronized (listeners) {
            if (listener != null && !listeners.contains(listener)) {
                listeners.add(listener);
            }
        }
    }

    public void unregisterListener(QueryPoolListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void updateListenersAdded(final RegexQuery query) {
        for (final QueryPoolListener listener: listeners) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    listener.onQueryAdded(query);
                }
            };
            runOnThread(runnable);
        }
    }

    private void updateListenersRemoved(final RegexQuery query) {
        for (final QueryPoolListener listener: listeners) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    listener.onQueryRemoved(query);
                }
            };
            runOnThread(runnable);
        }
    }

    private void runOnThread(Runnable runnable) {
        new Thread(runnable).run();
    }

}
