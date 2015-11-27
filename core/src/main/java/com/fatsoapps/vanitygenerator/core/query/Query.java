package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.search.SearchCase;
import com.fatsoapps.vanitygenerator.core.search.SearchPlacement;
import com.fatsoapps.vanitygenerator.core.network.Prefix;
import com.fatsoapps.vanitygenerator.core.tools.Utils;
import com.fatsoapps.vanitygenerator.networks.Network;
import org.bitcoinj.core.ECKey;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.fatsoapps.vanitygenerator.core.search.SearchCase.*;
import static com.fatsoapps.vanitygenerator.core.search.SearchPlacement.*;

/**
 * Query is an extension of RegexQuery which is meant to be an easier to use and more flexible Query type. Definitions
 * of each Query typically breaks down to a query string, position, case sensitivity, and an associated Prefix or
 * Network to search on. Query allows for each query input string to change, modification of where a query shows up in
 * a string, and case sensitivity.
 * @see com.fatsoapps.vanitygenerator.core.query.RegexQuery
 * @see com.fatsoapps.vanitygenerator.core.search.SearchPlacement
 * @see com.fatsoapps.vanitygenerator.core.search.SearchCase
 * @see com.fatsoapps.vanitygenerator.core.network.GlobalNetParams
 * @see com.fatsoapps.vanitygenerator.core.network.Prefix
 * @see com.fatsoapps.vanitygenerator.networks.Network
 */
public class Query extends RegexQuery {

    private String query;
    private SearchPlacement placement;
    private SearchCase searchCase;
    private Prefix[] associatedPrefixes;

    public Query(String query, boolean begins, boolean match, boolean compressed, Prefix prefix) {
        this(query, begins, match, compressed, false, prefix);
    }

    public Query(String query, boolean begins, boolean match, boolean compressed, boolean findUnlimited, Prefix prefix) {
        super(findUnlimited);
        this.placement = begins ? BEGINS : CONTAINS;
        this.searchCase = match ? MATCH : IGNORE;
        this.compressed = compressed;
        associatedPrefixes = new Prefix[] {prefix};
        if (begins) {
            query = getBeginsQuery(query);
        }
        this.query = query;
    }

    /**
     * Creates a Query with a supporting Network.
     * @param query - the query that should match Base58 before creation.
     * @param begins - determines placement
     * @param match - determines case sensitivity
     * @param compressed - determines compression type of ECKey
     * @param network - the pre-configured Network to set this Query to search against.
     */
    public Query(String query, boolean begins, boolean match, boolean compressed, Network network) {
        this(query, begins, match, compressed, false, network);
    }

    /**
     * Creates a Query with a supporting Network.
     * @param query - the query that should match Base58 before creation.
     * @param begins - determines placement
     * @param match - determines case sensitivity
     * @param compressed - determines compression type of ECKey
     * @param findUnlimited - determines whether this query should be removed once found
     * @param network - the pre-configured Network to set this Query to search against.
     * @see com.fatsoapps.vanitygenerator.core.query.QueryPool
     * @see com.fatsoapps.vanitygenerator.core.search.Search
     */
    public Query(String query, boolean begins, boolean match, boolean compressed, boolean findUnlimited, Network network) {
        this(query, begins ? BEGINS : CONTAINS, match ? MATCH : IGNORE, compressed, findUnlimited, network);
    }

    public Query(String query, SearchPlacement placement, SearchCase searchCase, boolean compressed, boolean findUnlimited, Network network) {
        super(findUnlimited);
        this.placement = placement;
        this.searchCase = searchCase;
        this.compressed = compressed;
        ArrayList<Prefix> prefixes = Prefix.getAddressPrefixes(network);
        associatedPrefixes = prefixes.toArray(new Prefix[prefixes.size()]);
        if (placement == BEGINS) {
            query = getBeginsQuery(query);
        }
        this.query = query;
    }

    private String getBeginsQuery(String query) {
        String beginning = "(";
        for (Prefix prefix: associatedPrefixes) {
            beginning += prefix.toString();
            if (prefix != associatedPrefixes[associatedPrefixes.length - 1]) {
                beginning += "|";
            }
        }
        beginning += ")";
        return beginning + query;
    }

    /**
     * Returns a Pattern / Regex formatted Query.
     */
    public String getQuery() {
        return query;
    }

    /**
     * Returns the raw version of the query string that was provided to make this Query.
     */
    public String getRawQuery() {
        return query.substring(query.lastIndexOf(')') + 1);
    }

    public SearchPlacement getPlacement() {
        return placement;
    }

    public SearchCase getSearchCase() {
        return searchCase;
    }

    public BigInteger getOdds() {
        return Utils.getOdds(getRawQuery(), placement == SearchPlacement.BEGINS, searchCase == SearchCase.MATCH);
    }

    public void updateNetwork(Network network) {
        updateSearchQuery(query, network);
    }

    public void updateSearchQuery(String input, Network network) {
        ArrayList<Prefix> prefixes = Prefix.getAddressPrefixes(network);
        associatedPrefixes = prefixes.toArray(new Prefix[prefixes.size()]);
        if (placement == BEGINS) {
            query = getBeginsQuery(input);
        } else {
            query = input;
        }
        updatePattern();
    }

    public void updatePrefix(Prefix prefix) {
        updateSearchQuery(query, prefix);
    }

    public void updateSearchQuery(String query, Prefix prefix) {
        associatedPrefixes = new Prefix[] {prefix};
        if (placement == BEGINS) {
            query = getBeginsQuery(query);
        }
        this.query = query;
        updatePattern();
    }

    public void updateMultiPrefixes(ArrayList<Prefix> prefixes) {
        updateMultiPrefixes(query, prefixes);
    }

    public void updateMultiPrefixes(String query, ArrayList<Prefix> prefixes) {
        associatedPrefixes = prefixes.toArray(new Prefix[prefixes.size()]);
        if (placement == BEGINS) {
            query = getBeginsQuery(query);
        }
        this.query = query;
        updatePattern();
    }

    public void setSearchPlacement(SearchPlacement placement) {
        this.placement = placement;
        if (placement == BEGINS) {
            query = getBeginsQuery(query);
        }
        updatePattern();
    }

    public void setSearchMode(SearchCase searchCase) {
        this.searchCase = searchCase;
        updatePattern();
    }

    /**
     * Check to see if this Query matches the ECKey in relation to netParams. Note: you do NOT need to handle
     * key compression since this method handles compression before checking.
     * @see org.bitcoinj.core.ECKey
     * @param key - ECKey that has been generated.
     * @param netParams - GNP that is being matched against.
     * @return whether the key matches this query or not.
     */
    public boolean matches(ECKey key, GlobalNetParams netParams) {
        if (pattern == null) {
            updatePattern();
        }
        return super.matches(key, netParams);
    }

    /**
     * Check to see if this Query matches this input. This should only be used when the thread calling this method is
     * handling the compression logic.
     * @param input - input to be matched against. Normally is a string form of an Address.
     * @return whether or not the input matches this address.
     * @see org.bitcoinj.core.Address
     */
    public boolean matches(String input) {
        if (pattern == null) {
            updatePattern();
        }
        return super.matches(input);
    }

    private void updatePattern() {
        pattern = getPattern();
    }

    private Pattern getPattern() {
        return Pattern.compile("^" + (searchCase == IGNORE ? "(?i)" : "") +
                (placement == CONTAINS ? ".*" : "") +
                query + ".*$");
    }

    public int hashCode() {
        int hash = 17;
        hash += 23 * (hash + query.hashCode());
        hash += 23 * (hash + ((searchCase == MATCH) ? 17 : 7));
        hash += 23 * (hash + ((placement == BEGINS) ? 17 : 7));
        hash += 23 * (hash + (compressed ? 17 : 7));
        hash += 23 * (hash + (findUnlimited ? 17 : 7));
        return hash;
    }

    public boolean equals(Query other) {
        return this.hashCode() == other.hashCode();
    }

}
