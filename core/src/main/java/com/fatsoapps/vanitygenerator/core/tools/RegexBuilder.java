package com.fatsoapps.vanitygenerator.core.tools;

import com.fatsoapps.vanitygenerator.core.query.Query;
import com.fatsoapps.vanitygenerator.core.search.SearchCase;
import com.fatsoapps.vanitygenerator.core.search.SearchPlacement;

import java.util.regex.Pattern;

/**
 * RegexBuilder is a static class that creates a regular expression string based off of an input of multiple queries
 * and the compression type desired.
 */
public class RegexBuilder {

    /**
     * Creates a regular expression string [Pattern] from an input of queries and a compression type. Please note that
     * there is no reason to combine compressed and uncompressed Pattern's (you do NOT want a compressed pattern
     * matching an uncompressed address and vice-versa).
     * @param compressed - creates a pattern only from queries matching this compression type.
     * @param queries - a list of queries to create a pattern from.
     * @return a Pattern used to check if a generated address matches any of the queries.
     */
    public static Pattern build(boolean compressed, Query... queries) {
        int index = 0;
        Query[] queriesAsArray = new Query[queries.length];
        for (Query query: queries) {
            if (query.isCompressed() == compressed) {
                queriesAsArray[index++] = query;
            }
        }
        return build(queriesAsArray);
    }

    private static Pattern build(Query[] queries) {
        StringBuilder regex = new StringBuilder("\\b^");
        regex.append(getSubPattern(SearchPlacement.BEGINS, queries));
        if (regex.length() > 4) {
            regex.append("|");
        }
        StringBuilder contains = getSubPattern(SearchPlacement.CONTAINS, queries);
        if (contains.length() == 0) {
            regex.deleteCharAt(regex.length() - 1);
        }
        regex.append(contains);
        regex.append(".*$");
        return Pattern.compile(regex.toString());
    }

    private static StringBuilder getSubPattern(SearchPlacement placement, Query[] queries) {
        StringBuilder subQuery = new StringBuilder("(");
        for (Query query: queries) {
            if (query == null) continue;
            if (query.getPlacement() == placement) {
                if (query.getSearchCase() == SearchCase.IGNORE) {
                    subQuery.append("(?i)");
                }
                if(query.getPlacement() == SearchPlacement.CONTAINS) {
                    subQuery.append(".*");
                }
                subQuery.append(query.getQuery()).append("|");
            }
        }
        if (subQuery.length() == 1) {
            subQuery = new StringBuilder();
        } else {
            subQuery.deleteCharAt(subQuery.length() - 1); // Remove the last |
            subQuery.append(")");
        }
        return subQuery;
    }


}
