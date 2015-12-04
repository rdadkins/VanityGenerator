package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.network.Prefix;
import com.fatsoapps.vanitygenerator.core.network.Network;
import com.fatsoapps.vanitygenerator.core.tools.Utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
 * @see com.fatsoapps.vanitygenerator.core.network.Network
 */
public class Query extends RegexQuery {

    private String query;
    private boolean begins;
    private boolean matchCase;

    private Query(QueryBuilder builder) {
        super(builder.compressed, builder.findUnlimited);
        this.begins = builder.beginsWith;
        this.matchCase = builder.matchCase;
        this.query = builder.query;
        updateNetwork(builder.network);
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public GlobalNetParams getNetworkParameters(GlobalNetParams netParams) {
        return netParams;
    }

    //TODO update this method to replace correct characters
    private void setPlacement(boolean begins) {
        this.begins = begins;
        if (!begins) {
            pattern = Pattern.compile("^.*" + pattern.toString().replace("^",""));
        } else {
            int index = pattern.toString().indexOf(')');
            pattern = Pattern.compile("^" + pattern.toString().substring(index + 1).replace("^",""));
        }
    }

    public void updateNetwork(Network network) {
        if (network == null) {
            updatePattern();
        } else {
            ArrayList<Prefix> prefixes = Prefix.getAddressPrefixes(network);
            updatePattern(prefixes.toArray(new Prefix[prefixes.size()]));
        }
    }

    public void updateNetwork(GlobalNetParams netParams) {
        updateNetwork(netParams.getNetwork());
    }

    public BigInteger getOdds() {
        return Utils.getOdds(query, begins, matchCase);
    }

    private void updatePattern(Prefix... prefixes) {
        if (begins && prefixes.length < 2) {
            pattern = Pattern.compile("^" + (matchCase ? "" : "(?i)") + "." + query + ".*$");
            return;
        }
        StringBuilder prefixBuilder = new StringBuilder("(");
        for (Prefix prefix: prefixes) {
            prefixBuilder.append(prefix.toString());
            if (prefix != prefixes[prefixes.length - 1]) {
                prefixBuilder.append("|");
            }
        }
        prefixBuilder.append(")");
        pattern = Pattern.compile("^" + prefixBuilder.toString() +
                (matchCase ? "" : "(?i)") + ".*" + query + ".*$");
    }

    public static class QueryBuilder {

        protected String query;
        protected boolean compressed;
        protected boolean findUnlimited;
        protected boolean beginsWith;
        protected boolean matchCase;
        protected Network network;

        public QueryBuilder(String query) {
            this.query = query;
        }

        public QueryBuilder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public QueryBuilder findUnlimited(boolean findUnlimited) {
            this.findUnlimited = findUnlimited;
            return this;
        }

        public QueryBuilder begins(boolean beginsWith) {
            this.beginsWith = beginsWith;
            return this;
        }

        public QueryBuilder matchCase(boolean matchCase) {
            this.matchCase = matchCase;
            return this;
        }

        public QueryBuilder network(Network network) {
            this.network = network;
            return this;
        }

        public Query build() {
            return new Query(this);
        }

    }

}
