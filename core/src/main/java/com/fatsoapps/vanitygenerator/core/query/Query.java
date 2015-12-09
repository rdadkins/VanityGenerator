package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.tools.Utils;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Query is an extension of RegexQuery which is meant to be an easier to use and more flexible Query type. Definitions
 * of each Query typically breaks down to a query string, position, and case sensitivity.
 */
public class Query extends RegexQuery {

    private String query;
    private boolean begins;
    private boolean matchCase;

    private Query(QueryBuilder builder) {
        super(builder.compressed, builder.findUnlimited, builder.searchForP2SH);
        this.begins = builder.beginsWith;
        this.matchCase = builder.matchCase;
        this.query = builder.query;
        updatePattern();
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    @Override
    public GlobalNetParams getNetworkParameters(GlobalNetParams netParams) {
        return netParams;
    }

    public void updateQuery(String query) throws Base58FormatException {
        Utils.checkBase58(query);
        this.query = query;
        updatePattern();
    }

    public void updatePlacement(boolean begins) {
        this.begins = begins;
        updatePattern();
    }

    public void updateMatchCase(boolean matchCase) {
        this.matchCase = matchCase;
        updatePattern();
    }

    public BigInteger getOdds() {
        return Utils.getOdds(query, begins, matchCase);
    }

    private void updatePattern() {
        pattern = Pattern.compile("^" + (begins ? "." : ".*") + (matchCase ? "" : "(?i)") + query + ".*$");
    }

    public static class QueryBuilder {

        protected String query;
        protected boolean compressed = true;
        protected boolean findUnlimited = false;
        protected boolean beginsWith = false;
        protected boolean matchCase = true;
        protected boolean searchForP2SH = false;

        /**
         * This is a builder class for Query. It is assumed that the query being passed in is already Base58 checked against.
         * If it is not, there is a risk of wasting CPU time searching for something that will never exist.
         * <br/>
         * <b>NOTE: If you are planning on creating a begins Query, the first letter of the address is not needed
         *      Example: user wants a Bitcoin address that begins with 1234 (with 1 being the Prefix of Bitcoin).
         *      You just need to provide 234 and set the begins() to true.</b>
         * @see com.fatsoapps.vanitygenerator.core.tools.Utils Utils.isBase58()
         * <br/>
         * @param query the pre-qualified Base58 string.
         */
        public QueryBuilder(String query) throws Base58FormatException {
            Utils.checkBase58(query);
            this.query = query;
        }

        /**
         * Set the compression of this query. When set to true, searching speeds will be at their peak performance while
         * false requires more computation before checking for matches. Default is set to true.
         * @param compressed the compression state.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        /**
         * Determine whether this Query should be found an unlimited amount of times while searching.
         * Default is set to false.
         * @param findUnlimited tells whether this Query should be removed from a collection while searching once found.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder findUnlimited(boolean findUnlimited) {
            this.findUnlimited = findUnlimited;
            return this;
        }

        /**
         * Indicates whether the matching should be restricted to the beginning of an address or throughout.
         * <br/>Set to true means that the query should be found at the beginning of an address
         * <br/>Example: query = test. Found = 1test...
         * <br/>Set to false means that the query should be found anywhere in the address
         * <br/>Example: query = test. Found = 1...test...
         * <br/><b>When setting this to true, you do not need to consider the Prefix of the address as a letter. This means
         * that if you want a begins query with 1234, the 1 does not need to be included if you are only interested in the
         * 234 part. You just need to define 234 and set this to begins.</b>
         * @param beginsWith determines the placement of the query when searching.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder begins(boolean beginsWith) {
            this.beginsWith = beginsWith;
            return this;
        }

        /**
         * Determines the case sensitivity when searching.
         * <br/>Set to true means that a query must match the case in which it was provided. ABC == ABC in this case.
         * <br/>Set to false means that a query can match any case. ABC == aBc in this case.
         * @param matchCase determines the case sensitivity when searching.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder matchCase(boolean matchCase) {
            this.matchCase = matchCase;
            return this;
        }

        /**
         * Indicates if the address that needs to be searched for should be a P2SH (Pay to Script Hash) address.
         * @param searchForP2SH determines whether to search for a P2SH address or not.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder searchForP2SH(boolean searchForP2SH) {
            this.searchForP2SH = searchForP2SH;
            return this;
        }

        /**
         * Build this QueryBuilder into a Query
         * @return the Query from this QueryBuilder.
         */
        public Query build() {
            return new Query(this);
        }

    }

}
