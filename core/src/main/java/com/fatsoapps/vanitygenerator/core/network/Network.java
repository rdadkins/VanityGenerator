package com.fatsoapps.vanitygenerator.core.network;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Network is a collection of defined crypto currencies with their pre-configured address header, private key
 * headers, and an optional P2SH header. Since this list can get quite large, it would be wise to exclude it from
 * mobile based apps due to restricted memory space.
 */
public enum Network {

    BITCOIN(0, 5, 128),
    BITCOIN_TEST(111, 196, 239),
    LITECOIN(48, 5, 176),
    DASHCOIN(76, 16, 204),
    DOGECOIN(30, 22, 158),
    MULTI(2, 0, 0);

    private int addressHeader;
    private int privateKeyHeader;
    private int p2shHeader;

    Network(int addressHeader, int p2shHeader, int privateKeyHeader) throws ExceptionInInitializerError {
        checkInRange(addressHeader);
        checkInRange(p2shHeader);
        checkInRange(privateKeyHeader);
        this.addressHeader = addressHeader;
        this.p2shHeader = p2shHeader;
        this.privateKeyHeader = privateKeyHeader;
    }

    public int getAddressHeader() {
        return addressHeader;
    }

    public int getPrivateKeyHeader() {
        return privateKeyHeader;
    }

    public int getP2shHeader() {
        return p2shHeader;
    }

    public static ArrayList<String> getNetworkNames() {
        ArrayList<String> networksAsString = new ArrayList<String>();
        for (Network network: values()) {
            networksAsString.add(network.name().replace("_", " "));
        }
        return networksAsString;
    }

    @Nullable
    public static Network networkFromAddressHeader(int addressHeader) {
        Network network = null;
        for (Network nw: values()) {
            if (nw.addressHeader == addressHeader) {
                network = nw;
                break;
            }
        }
        return network;
    }

    /**
     * Derives a Network from an addressHeader and a privateKeyHeader. Returns null if the pair does not exist.
     */
    @Nullable
    public static Network deriveFrom(int addressHeader, int privateKeyHeader) {
        Network network = null;
        for (Network nw: values()) {
            if (nw.addressHeader == addressHeader && nw.privateKeyHeader == privateKeyHeader) {
                network = nw;
                break;
            }
        }
        return network;
    }

    private void checkInRange(int value) throws ExceptionInInitializerError {
        if (value < 0 || value > 255) {
            throw new ExceptionInInitializerError("Illegal decimal value: " + value + ". Value must be in range of [0, 255].");
        }
    }

}
