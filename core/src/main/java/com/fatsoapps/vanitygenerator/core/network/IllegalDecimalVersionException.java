package com.fatsoapps.vanitygenerator.core.network;

public class IllegalDecimalVersionException extends RuntimeException {

    public IllegalDecimalVersionException(int version) {
        super("Illegal Decimal Version: " + version + ". Must be in range [0, 255].");
    }

}
