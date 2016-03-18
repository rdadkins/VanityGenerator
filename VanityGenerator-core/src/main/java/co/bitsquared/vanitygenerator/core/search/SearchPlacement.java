package co.bitsquared.vanitygenerator.core.search;

/**
 * SearchPlacement is the definition of where a Search thread is looking for a query.
 * BEGINS: The Search thread is looking for the Query at the beginning of an address.
 *      123: 123...
 * CONTAINS: The Search thread is looking for the Query anywhere in the address
 *      123: 1...123...
 * Can be combined with SearchCase to further customize searching.
 */
public enum SearchPlacement {

    BEGINS,
    CONTAINS

}
