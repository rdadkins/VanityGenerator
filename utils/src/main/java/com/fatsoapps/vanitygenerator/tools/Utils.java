package com.fatsoapps.vanitygenerator.tools;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class Utils {

    public static String formatWithCommas(long value) {
        return formatWithCommas(BigInteger.valueOf(value));
    }

    public static String formatWithCommas(BigInteger value) {
        DecimalFormat format = new DecimalFormat("#,###");
        return format.format(value);
    }

    public static String capitalize(String input) {
        String formatted;
        if (input != null) {
            formatted = input.substring(0, 1).toUpperCase();
            if (input.length() > 1) {
                formatted += input.substring(1).toLowerCase();
            }
        } else {
            formatted = "";
        }
        return formatted;
    }

}
