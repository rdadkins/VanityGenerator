package com.fatsoapps.vanitygenerator.core.search;

public enum SearchMode {

    /**
     * Search for every query provided at once. This will have the worst performance in regards to other search modes and
     * will only get worse when there are more queries to search on.
     */
    SEARCH_ALL,

    /**
     * Search for one query at a time in terms of difficulty from easiest to hardest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    EASIEST_HARDEST,

    /**
     * Search for one query at a time in terms of difficulty from hardest to easiest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    HARDEST_EASIEST,

    /**
     * Search for one query at a time in terms of length from shortest to longest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    SHORTEST_LONGEST,

    /**
     * Search for one query at a time in terms of length from longest to shortest. Due to the semantics of this search mode,
     * findUnlimited setting of each query will be ignored meaning that when a query is found, it will be removed from the collection.
     */
    LONGEST_SHORTEST;

}
