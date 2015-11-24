package com.fatsoapps.vanitygenerator.core.network;

public class IllegalDecimalVersionException extends Exception {

    public IllegalDecimalVersionException(int version) {
        super("Illegal Decimal Version: " + version + ". Must be in range [0, 255].");
    }

}
