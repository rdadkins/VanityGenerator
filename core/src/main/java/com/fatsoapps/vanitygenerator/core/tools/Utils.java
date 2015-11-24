package com.fatsoapps.vanitygenerator.core.tools;

import java.math.BigInteger;

/**
 * Utils for the Vanity Generator Core
 */
public class Utils {

    public static final int BASE = 58;
    public static final char[] NON_REPEAT_CHARS = new char[]{'i', 'L', 'o'};

    /**
     * Calculates the estimated number of address needed to be generated to obtain an address that contains query.
     * Cases are as follows:
     *      Case    |   Placement   |   Outcome
     *      Match   |   Begins      |   58 ^ (length - 1)
     *      Match   |   Contains    |   58 ^ (length)
     *      Ignore  |   Contains    |   For each character (multiply):
     *                                      If it is in NON_REPEAT_CHARS: 58
     *                                      Else: 29 (58 / 2)
     *      Ignore  |   Begins      |   Same as Ignore Contains except you divide by 29 or 58 depending on uniqueness.
     * @return estimated number of addresses to be generated.
     */
    public static BigInteger getOdds(String query, boolean begins, boolean match) {
        BigInteger amount;
        if (match) {
            amount = new BigInteger(String.valueOf(BASE));
            if (begins) {
                amount = amount.pow(query.length() - 1);
            } else {
                amount = amount.pow(query.length());
            }
        } else {
            long estimated = 1;
            for (char c: query.toCharArray()) {
                if (containsUniqueChar(c)) {
                    estimated *= BASE; // Unique character which is 1 / 58
                } else {
                    estimated *= (BASE / 2); // Ex: A or a | B or b etc.
                }
            }
            if (begins) {
                if (containsUniqueChar(query.charAt(0))) {
                    estimated /= 58;
                } else {
                    estimated /= 29;
                }
            }
            amount = BigInteger.valueOf(estimated);
        }
        return amount;
    }

    private static boolean containsUniqueChar(char c) {
        boolean contains = false;
        for (char nrc: NON_REPEAT_CHARS) {
            if (c == nrc) {
                contains = true;
                break;
            }
        }
        return contains;
    }

}
