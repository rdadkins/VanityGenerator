package co.bitsquared.vanitygenerator.core.listeners;

import co.bitsquared.vanitygenerator.core.query.RegexQuery;

public interface QueryPoolListener {

    /**
     * Called when a query is removed from the pool.
     * @param query the query that was removed.
     */
    void onQueryRemoved(RegexQuery query);

    /**
     * Called when a query is added to the pool.
     * @param query the query that was added.
     */
    void onQueryAdded(RegexQuery query);

}
