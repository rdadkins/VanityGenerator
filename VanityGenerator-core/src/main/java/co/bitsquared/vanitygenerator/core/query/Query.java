package co.bitsquared.vanitygenerator.core.query;

import co.bitsquared.vanitygenerator.core.exceptions.Base58FormatException;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.tools.Utils;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.PriorityQueue;
import java.util.regex.Pattern;

/**
 * Query is an extension of {@code RegexQuery} which is meant to be an easier to use and more flexible Query type. Definitions
 * of each Query typically breaks down to a query string, position, and case sensitivity. The only way to create a Query
 * is through {@code QueryBuilder}.
 *
 * @see co.bitsquared.vanitygenerator.core.query.RegexQuery
 * @see co.bitsquared.vanitygenerator.core.query.Query.QueryBuilder
 */
public class Query extends RegexQuery implements Comparable<RegexQuery> {

    private String query;
    private boolean begins;
    private boolean matchCase;

    protected Query(QueryBuilder builder) {
        super(builder.compressed, builder.findUnlimited, builder.searchForP2SH);
        this.begins = builder.beginsWith;
        this.matchCase = builder.matchCase;
        this.query = builder.query;
        this.netParams = builder.netParams;
        updatePattern();
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

    public String getPlainQuery() {
        return query;
    }

    public boolean isBegins() {
        return begins;
    }

    public boolean isMatchCase() {
        return matchCase;
    }

    public BigInteger getOdds() {
        return Utils.getOdds(query, begins, matchCase);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash *= 23 + (begins ? 1 : 0);
        hash *= 23 + (matchCase ? 1 : 0);
        hash *= 23 + (compressed ? 1 : 0);
        hash *= 23 + (findUnlimited ? 1 : 0);
        hash *= 23 + (searchForP2SH ? 1 : 0);
        hash *= 23 + query.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Query && other.hashCode() == hashCode();
    }

    private void updatePattern() {
        pattern = Pattern.compile("^" + (begins ? "." : ".*") + (matchCase ? "" : "(?i)") + query + ".*$");
    }

    /**
     * This method compares this Query with another Query and the sorting output should represent the easiest to hardest
     * searching order on a collection of Query's (i.e., the smallest value is the easiest to find).
     * Sorting depends on these properties in order:
     * Query Length -> Compression -> Begins -> MatchCase
     * Query Length:
     *      Match: Check Compression
     *      Don't match: return difficulty comparison
     * Compression:
     *      Match: Check Begins
     *      Don't match: if this compression is true, return -1 since searching for compressed addresses is faster. Otherwise return 1.
     * Begins:
     *      Match: Check MatchCase
     *      Don't match: if this query is begins, return 1 since it is harder to find a query that begins with an expression. Otherwise return -1.
     * MatchCase:
     *      Match: return 0 since these Query's are identical in the greater sense.
     *      Don't match: if this query is match case, return 1 since it is harder to find a query matching exact letter casing. Otherwise return -1.
     */
    @Override
    public int compareTo(@Nonnull RegexQuery other) {
        if (!(other instanceof Query)) return 0;
        Query otherQuery = (Query) other;
        if (hashCode() == otherQuery.hashCode()) return 0;
        int lengthDifference = query.length() - otherQuery.query.length();
        if (lengthDifference == 0) {
            if (compressed == otherQuery.compressed) {
                if (begins == otherQuery.begins) {
                    if (matchCase == otherQuery.matchCase) {
                        return 0;
                    }
                    return matchCase ? 1 : -1;
                }
                return begins ? 1 : -1;
            }
            return compressed ? -1 : 1;
        } else {
            return getDifficulty().compareTo(otherQuery.getDifficulty());
        }
    }

    public BigInteger getDifficulty() {
        return Utils.getOdds(query, begins, matchCase);
    }

    public static class QueryBuilder {

        private String query;
        private boolean compressed = true;
        private boolean findUnlimited = false;
        private boolean beginsWith = false;
        private boolean matchCase = true;
        private boolean searchForP2SH = false;
        private GlobalNetParams netParams;

        /**
         * This is a builder class for Query. It is assumed that the query being passed in is already Base58 checked against.
         * If it is not, there is a risk of wasting CPU time searching for something that will never exist.
         * <br/>
         * <b>NOTE: If you are planning on creating a begins Query, the first letter of the address is not needed
         *      Example: user wants a Bitcoin address that begins with 1234 (with 1 being the Prefix of Bitcoin).
         *      You just need to provide 234 and set the begins() to true.</b>
         * @see Utils Utils.isBase58()
         * <br/>
         * @param query the plain text query that must match Base58
         * @throws Base58FormatException if the query supplied does not match Base58
         */
        public QueryBuilder(String query) {
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
         * Set the GlobalNetParams for this Query. If there is no network provided, all matches will use the incoming
         * GlobalNetParams.
         * @param netParams the desired network for this query.
         * @return the instance of this QueryBuilder.
         */
        public QueryBuilder targetNetwork(GlobalNetParams netParams) {
            this.netParams = netParams;
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
