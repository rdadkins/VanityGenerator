package co.bitsquared.vanitygenerator.core.tools;

import co.bitsquared.vanitygenerator.core.exceptions.IllegalDecimalVersionException;
import co.bitsquared.vanitygenerator.core.exceptions.Base58FormatException;

import java.math.BigInteger;
import java.util.regex.Pattern;

/**
 * Utils for Vanity Generator Core
 */
public class Utils {

    public static final int BASE = 58;
    public static final char[] NON_REPEAT_CHARS = new char[]{'i', 'L', 'o'};
    public static final Pattern BASE_58 = Pattern.compile("^[123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz]*$");

    /**
     * This is a pseudo odds calculator. This returns a number based off of search case, search placement, and the
     * length of the query. The estimates from this method will not be accurate since this doesn't dive in to the Base58
     * pattern encoding scheme.
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

    public static boolean isBase58(String input) {
        return BASE_58.matcher(input).find();
    }

    public static void checkBase58(String input) throws Base58FormatException {
        if (!isBase58(input)) {
            throw new Base58FormatException(input);
        }
    }

    /**
     * Checks to see if the input integer is within the valid range of decimal's. The valid decimal range is within the
     * closed range [0, 255].
     * @param input - integer to check if it is in valid range.
     * @throws IllegalDecimalVersionException if the input is out of range.
     */
    public static void checkIfValidDecimal(int input) {
        if (input < 0 || input > 255) {
            throw new IllegalDecimalVersionException(input);
        }
    }

}
