package co.bitsquared.vanitygenerator.core.exceptions;

public class Base58FormatException extends RuntimeException {

    public Base58FormatException(String input) {
        super(input + " is not valid Base58.");
    }

}
