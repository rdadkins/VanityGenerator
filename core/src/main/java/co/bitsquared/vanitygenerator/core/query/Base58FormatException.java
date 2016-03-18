package co.bitsquared.vanitygenerator.core.query;

public class Base58FormatException extends Exception {

    public Base58FormatException(String input) {
        super(input + " is not valid Base58.");
    }

}
