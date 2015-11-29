package com.fatsoapps.vanitygenerator.core.tools;

import java.math.BigInteger;

/**
 * Utils for the Vanity Generator Core
 */
public class Utils {

    public static final int BASE = 58;
    public static final char[] NON_REPEAT_CHARS = new char[]{'i', 'L', 'o'};

    public static void main(String[] args) {
        System.out.println(getOdds("abc", false, false));
    }

    /**
     * Calculates the estimated number of address needed to be generated to obtain an address that contains query.
     * Cases are as follows:
     *      Case    |   Placement   |   Outcome
     *      Match   |   Begins      |   58 ^ (length)
     *      Match   |   Contains    |   58 ^ (length) / 32 (semi-average length not including prefix of an address)
     *      Ignore  |   Contains    |   For each character (multiply):
     *                                      If it is in NON_REPEAT_CHARS: 58
     *                                      Else: 29 (58 / 2)
     *                                      Finally divide by 32
     *      Ignore  |   Begins      |   Same as Ignore Contains except you divide by 29 or 58 depending on uniqueness.
     * @return estimated number of addresses to be generated.
     */
    public static BigInteger getOdds(String query, boolean begins, boolean match) {
        BigInteger amount;
        if (match) {
            amount = new BigInteger(String.valueOf(BASE)).pow(query.length());
        } else {
            amount = BigInteger.ONE;
            for (char c: query.toCharArray()) {
                if (containsUniqueChar(c)) {
                    amount = amount.multiply(BigInteger.valueOf(BASE)); // Unique character which is 1 / 58
                } else {
                    amount = amount.multiply(BigInteger.valueOf(BASE).divide(BigInteger.valueOf(2))); // Ex: A or a | B or b etc.
                }
            }
        }
        if (!begins) {
            amount = amount.divide(BigInteger.valueOf(32));
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
