package co.bitsquared.vanitygenerator.core.search;

/**
 * SearchCase is the definition of whether or not a Search thread cares that if the Query is case sensitive.
 * MATCH: The Search thread is looking for an exact match of the Query
 *      CheeSE: ...CheeSE...
 * IGNORE: The Search thread does not care about the case of the Query
 *      CheeSE: ...chEEsE...
 * Can be combined with SearchPlacement to further customize searching.
 */
public enum SearchCase {

    MATCH, IGNORE

}
