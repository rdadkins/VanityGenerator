package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.IllegalDecimalVersionException;
import com.fatsoapps.vanitygenerator.networks.Network;
import com.fatsoapps.vanitygenerator.core.network.Prefix;
import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.search.SearchPlacement;
import com.fatsoapps.vanitygenerator.core.tools.RegexBuilder;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * QueryPool is collection of queries defined by the user that can be accessed anywhere by calling getInstance().
 * General usage is to create threads that rely on the Query's within QueryPool and to add / update / delete
 * accordingly.
 * @see com.fatsoapps.vanitygenerator.core.search.PoolSearch to see usage of QueryPool.
 */
public class QueryPool {

    private Network network;
    private final ArrayList<Query> queries;
    private Pattern pattern;
    private Pattern uncompressedPattern;
    private GlobalNetParams netParams;
    private int desiredThreadCount;
    private int maxThreadCount;
    private boolean containsCompressed;
    private boolean containsCompressedUpdated = false;
    private boolean containsUncompressed;
    private boolean containsUncompressedUpdated = false;

    private static QueryPool instance;

    /**
     * Tries to return the instance of QueryPool. If it doesn't exist, an exception is thrown.
     * @return instance of QueryPool.
     * @throws Exception if instance does not exist.
     */
    public static synchronized QueryPool getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Instance does not exist!");
        }
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
        queries = new ArrayList<Query>();
        netParams = GlobalNetParams.getTempInstance(network);
        this.network = network;
    }

    private QueryPool(int publicKeyHeader, int p2shHeader, int privateKeyHeader) throws IllegalDecimalVersionException {
        queries = new ArrayList<Query>();
        netParams = GlobalNetParams.getTempInstance(publicKeyHeader, p2shHeader, privateKeyHeader);
    }

    public synchronized void addQuery(Query query) {
        if (queries.contains(query)) return;
        queries.add(query);
        updateParameters();
    }

    public synchronized void removeQuery(int originalHashCode) {
        for (Query query: queries) {
            if (query.hashCode() == originalHashCode) {
                removeQuery(query);
                break;
            }
        }
    }

    public synchronized void removeQuery(Query query) {
        if (query == null) return;
        queries.remove(query);
        updateParameters();
    }

    public synchronized void updateQuery(Query newQuery, int originalHashCode) {
        if (newQuery.hashCode() == originalHashCode || contains(newQuery)) return;
        int index = -1;
        for (int i = 0; i < queries.size(); i++) {
            if (queries.get(i).hashCode() == originalHashCode) {
                index = i;
                break;
            }
        }
        if (index == -1) return;
        queries.set(index, newQuery);
        updateParameters();
    }

    public boolean contains(Query query) {
        boolean contains = false;
        for (Query q: queries) {
            if (q.hashCode() == query.hashCode()) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    private void updateParameters() {
        updateContainsCompressedType(true);
        updateContainsCompressedType(false);
        updateCompressedPattern();
        updateUncompressedPattern();
    }

    public boolean containsQueries() {
        return queries.size() > 0;
    }

    public boolean containsCompressedQueries() {
        if (containsCompressedUpdated) {
            updateContainsCompressedType(true);
            containsCompressedUpdated = false;
        }
        return containsCompressed;
    }

    public boolean containsUncompressedQueries() {
        if (containsUncompressedUpdated) {
            updateContainsCompressedType(false);
            containsUncompressedUpdated = false;
        }
        return containsUncompressed;
    }

    private void updateContainsCompressedType(boolean compressedType) {
        boolean containsType = false;
        for (Query query: queries) {
            if (query.isCompressed() == compressedType) {
                containsType = true;
                break;
            }
        }
        if (compressedType) {
            containsCompressed = containsType;
        } else {
            containsUncompressed = containsType;
        }
    }

    public int getAmountOfQueries() {
        return queries.size();
    }

    public synchronized Pattern getPattern() {
        if (containsCompressedUpdated || pattern == null) {
            System.out.println("Updating pattern");
            updateCompressedPattern();
            containsCompressedUpdated = false;
        }
        return pattern;
    }

    private void updateCompressedPattern() {
        pattern = RegexBuilder.build(true, queries.toArray(new Query[queries.size()]));
    }

    public synchronized Pattern getUncompressedPattern() {
        if (containsUncompressedUpdated || uncompressedPattern == null) {
            updateUncompressedPattern();
            containsUncompressedUpdated = false;
        }
        return uncompressedPattern;
    }

    private void updateUncompressedPattern() {
        uncompressedPattern = RegexBuilder.build(false, queries.toArray(new Query[queries.size()]));
    }

    /**
     * When a user decides to change the Network they are searching on, all of the queries need to be updated with
     * the new updated Network
     * @param network - Network that is going to replace the old Network.
     */
    public void updateNetwork(Network network) {
        if (this.network != network) {
            this.network = network;
            updateNetwork(GlobalNetParams.getTempInstance(network));
        }
    }

    /**
     * When a user decides to change the GlobalNetParams they are searching on, all of the queries need to be updated
     * with the new GNP. This method is used over updateNetwork(Network) when the networks module is excluded.
     * @param netParams - GNP that is going to replace the old GNP.
     */
    public void updateNetwork(GlobalNetParams netParams) {
        if (this.netParams.getAddressHeader() != netParams.getAddressHeader()) {
            this.netParams = netParams;
            ArrayList<Prefix> prefixes = netParams.getAssociatedPrefixes();
            for (Query query: queries) {
                query.updateMultiPrefixes(prefixes);
            }
        }
    }

    /**
     * Updates the collection of Query's in this pool by removing the most complex Query that matches this pattern.
     * Complexity comes into place when multiple Query's match the pattern that has been found, length has top priority,
     * then BEGINS Query's and then CONTAINS Query's.
     * @param patternFound - the pattern that has been found within this pool.
     * @see com.fatsoapps.vanitygenerator.core.search.SearchPlacement
     */
    public void updateQueryList(String patternFound, boolean compressed) {
        ArrayList<Query> matchedQueries = new ArrayList<Query>();
        for (Query query: queries) {
            if (!query.isFindUnlimited() && query.isCompressed() == compressed && query.matches(patternFound)) {
                matchedQueries.add(query);
            }
        }
        while (matchedQueries.size() > 1) {
            removeLeastComplexQuery(matchedQueries);
        }
        for (Query matched: matchedQueries) { // We should end up with one, but this is here to prevent any exceptions
            queries.remove(matched);
        }
        updateParameters();
    }

    /**
     * Handles a list of matched queries from a pattern that has been found. If every Query that has been found is
     * either all BEGINS or CONTAINS, we remove the shortest (least complex) Query from the list. Otherwise, if there
     * is a mixture of BEGINS and CONTAINS Query's, we remove the first CONTAINS (least complex) Query first, if that
     * does not exist (which it should always exist) than we remove the first BEGINS Query.
     * TL;DR: We focus on the least complex type to remove.
     * Case 1: 3 BEGINS Query's
     *      Pattern found: 1Vanityabc123
     *      Query 1: 1Vanity...
     *      Query 2: 1Vanityabc...
     *      Query 3: 1Vanityabc123...
     *      Since this is a case where every Query is a BEGINS type, we remove the shortest (least complex) Query from
     *      the list which will be number 1. Since this method will be called until the list in question has only one
     *      remaining Query, Query 3 will be the last remaining (most complex) Query that needs to be removed from the
     *      list. This is the outcome because number 1 and 2 will be easier to find than number 3.
     * Case 2: 3 CONTAINS Query's
     *      Pattern found: 1...Vanity...
     *      Query 1: 1...Van...
     *      Query 2: 1...Vanit...
     *      Query 3: 1...Vanity...
     *      This will resemble the same outcome as case 1 where we end up with the most complex (number 3) as the
     *      remaining victor.
     * Case 3: CONTAINS and BEGINS Query's
     *      Pattern found: 123abc
     *      Query 1 [CONTAINS]: 1...123abc...
     *      Query 2 [BEGINS]: 123abc...
     *      Query 3 [BEGINS]: 123ab...
     *      Query 4 [CONTAINS]: 1...123ab...
     *      Since this list is neither all of the same type, we focus on the least complex type (CONTAINS) to remove
     *      first. The order of removal is 4, 1, 3. We remove 4 since it is the shortest least complex of the four.
     *      On the next iteration, we remove number 1 since it is harder to find a BEGINS Query, then we remove number 3
     *      since it is the shorter of the two BEGINS Query's remaining. Number 4 is not removed since we only call this
     *      method until there is one Query remaining.
     * @param matchedQueries - a list of matched Query's that needs to be shrunk.
     */
    private void removeLeastComplexQuery(ArrayList<Query> matchedQueries) {
        boolean allBegins = areAllType(matchedQueries, SearchPlacement.BEGINS);
        boolean allContains = areAllType(matchedQueries, SearchPlacement.CONTAINS);
        if (allBegins || allContains) {
            removeShortestQuery(matchedQueries);
        } else {
            int sizeBeforeRemoval = matchedQueries.size();
            removeShortestType(matchedQueries, SearchPlacement.CONTAINS);
            if (sizeBeforeRemoval == matchedQueries.size()) {
                removeShortestType(matchedQueries, SearchPlacement.BEGINS);
            }
        }
    }

    private boolean areAllType(ArrayList<Query> queries, SearchPlacement placement) {
        boolean isType = true;
        for(Query q: queries) {
            isType &= q.getPlacement() == placement;
        }
        return isType;
    }

    private void removeShortestQuery(ArrayList<Query> matchedQueries) {
        Query shortest = null;
        int length = Integer.MAX_VALUE;
        for (Query query: matchedQueries) {
            if (query.getRawQuery().length() < length) {
                length = query.getRawQuery().length();
                shortest = query;
            }
        }
        matchedQueries.remove(shortest);
    }

    private void removeShortestType(ArrayList<Query> matchedQueries, SearchPlacement placement) {
        Query mQuery = null;
        int shortestLength = Integer.MAX_VALUE;
        for (Query query: matchedQueries) {
            if (query.getPlacement() == placement && query.getRawQuery().length() < shortestLength) {
                mQuery = query;
                shortestLength = query.getRawQuery().length();
            }
        }
        matchedQueries.remove(mQuery);
    }

    public ArrayList<Query> getQueries() {
        return queries;
    }

    //TODO remove this methods and provide an overridden android class that supplies these methods.
    public void setDesiredThreadCount(int desired) {
        desiredThreadCount = desired;
    }

    public void setMaxThreadCount(int max) {
        maxThreadCount = max;
    }

    public int getMaxThreadCount() {
        return maxThreadCount;
    }

    public int getDesiredThreadCount() {
        return desiredThreadCount;
    }

    public Network getNetwork() {
        return network;
    }
}
