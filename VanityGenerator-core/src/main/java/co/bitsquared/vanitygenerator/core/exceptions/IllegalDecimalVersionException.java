package co.bitsquared.vanitygenerator.core.exceptions;

/**
 * IllegalDecimalVersionException is raised when an integer is being checked if it is in a valid range [0, 255] for
 * creating addresses. The Bitcoin protocol only accepts the closed range of [0, 255], so, anything outside of the range
 * will throw an exception later down the line when trying to convert an ECKey to an Address.
 */
public class IllegalDecimalVersionException extends RuntimeException {

    public IllegalDecimalVersionException(int version) {
        super("Illegal Decimal Version: " + version + ". Must be in range [0, 255].");
    }

}
