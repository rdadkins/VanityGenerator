package com.fatsoapps.vanitygenerator.core.network;

import com.fatsoapps.vanitygenerator.core.tools.Utils;
import org.bitcoinj.core.ECKey;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Network is a collection of defined crypto currencies with their pre-configured address header, private key
 * headers, and an optional P2SH header.
 */
public enum Network {

    BITCOIN(0, 5, 128),
    BITCOIN_TEST(111, 196, 239),
    LITECOIN(48, 5, 176),
    DASHCOIN(76, 16, 204),
    DOGECOIN(30, 22, 158),
    MULTI(2, 0);

    private int addressHeader;
    private int privateKeyHeader;
    private int p2shHeader = -1;

    Network(int addressHeader, int p2shHeader, int privateKeyHeader) {
        this(addressHeader, privateKeyHeader);
        Utils.checkIfValidDecimal(p2shHeader);
        this.p2shHeader = p2shHeader;
    }

    Network(int addressHeader, int privateKeyHeader) {
        Utils.checkIfValidDecimal(addressHeader);
        Utils.checkIfValidDecimal(privateKeyHeader);
        this.addressHeader = addressHeader;
        this.privateKeyHeader = privateKeyHeader;
    }

    public int getAddressHeader() {
        return addressHeader;
    }

    public int getPrivateKeyHeader() {
        return privateKeyHeader;
    }

    public int getP2shHeader() throws Exception {
        if (p2shHeader == -1) {
            throw new Exception(String.format("P2SH Header not initialized; %s does not define a P2SH Header.", name()));
        }
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

    @Nullable
    public static Network deriveFrom(int addressHeader, int privateKeyHeader, int p2shHeader) {
        Network network = null;
        for (Network nw: values()) {
            if (nw.addressHeader == addressHeader && nw.privateKeyHeader == privateKeyHeader && p2shHeader == nw.p2shHeader) {
                network = nw;
                break;
            }
        }
        return network;
    }

    public GlobalNetParams toGlobalNetParams() {
        return new GlobalNetParams(this);
    }

}
