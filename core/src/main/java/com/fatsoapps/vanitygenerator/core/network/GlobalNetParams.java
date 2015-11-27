package com.fatsoapps.vanitygenerator.core.network;

import com.fatsoapps.vanitygenerator.networks.Network;
import org.bitcoinj.core.NetworkParameters;

import javax.annotation.Nullable;
import java.util.ArrayList;


/**
 * GlobalNetParams is an extension of NetworkParameters. GlobalNetParams allows a user to define a decimal
 * version, other than the default 0, to create an address with a prefix defined by that decimal version (see more at
 * https://en.bitcoin.it/wiki/List_of_address_prefixes). A user must also define a privateKeyHeader that corresponds
 * with the decimal version which is typically found in chainparams.cpp [base58Prefixes] within the source code of
 * their desired coin. An optional script header can be used as well. This class is only meant to contain headers of
 * different networks, so this does not contain items such as port configuration.
 * NOTE: This class is similar to org.bitcoinj.core.Context in bitcoinj 0.13+ but Context will not be used since this
 * class was under development before Context's debut, and it is not readily available for use.
 * @see org.bitcoinj.core.ECKey
 * @see org.bitcoinj.core.Address
 */
public class GlobalNetParams extends NetworkParameters {

    private static GlobalNetParams instance;
    private Network network;

    private GlobalNetParams(Network network) {
        this.network = network;
        addressHeader = network.getAddressHeader();
        dumpedPrivateKeyHeader = network.getPrivateKeyHeader();
        p2shHeader = network.getP2shHeader();
        acceptableAddressCodes = new int[] {addressHeader, dumpedPrivateKeyHeader, p2shHeader};
    }

    private GlobalNetParams(int decimalVersion, int privateKeyHeader) throws IllegalDecimalVersionException {
        this(decimalVersion, privateKeyHeader, 0);
    }

    private GlobalNetParams(int addressHeader, int dumpedPrivateKeyHeader, int p2shHeader) throws IllegalDecimalVersionException {
        checkDecimal(addressHeader);
        checkDecimal(dumpedPrivateKeyHeader);
        checkDecimal(p2shHeader);
        this.addressHeader = addressHeader;
        this.dumpedPrivateKeyHeader = dumpedPrivateKeyHeader;
        this.p2shHeader = p2shHeader;
        acceptableAddressCodes = new int[] {this.addressHeader, p2shHeader};
        network = Network.deriveFrom(addressHeader, dumpedPrivateKeyHeader);
    }

    /**
     * Get a temporary instance of GNP that doesn't conflict with the current instance.
     * @param decimalVersion - The address header version, defines the first letter of an address.
     * @param privateKeyHeader - Defines the private key format.
     * @return a temporary instance of GNP.
     * @throws IllegalDecimalVersionException if decimalVersion or privateKeyHeader are not in [0, 255]
     */
    public static GlobalNetParams getTempInstance(int decimalVersion, int privateKeyHeader) throws IllegalDecimalVersionException {
        return new GlobalNetParams(decimalVersion, privateKeyHeader);
    }

    /**
     * Get a temporary instance of GNP that doesn't conflict with the current instance.
     * @param decimalVersion - Defines the first letter of an address.
     * @param privateKeyHeader - Defines the private key format.
     * @param p2shHeader - Defines the address header used for P2SH addresses.
     * @return a temporary instance of GNP.
     * @throws IllegalDecimalVersionException if decimalVersion or privateKeyHeader are not in [0, 255]
     */
    public static GlobalNetParams getTempInstance(int decimalVersion, int privateKeyHeader, int p2shHeader) throws IllegalDecimalVersionException {
        return new GlobalNetParams(decimalVersion, privateKeyHeader, p2shHeader);
    }

    /**
     * Get a temporary instance of GNP that doesn't conflict with the current instance.
     * @param network - the pre-defined network to configure GNP.
     * @return a temporary instance of GNP.
     */
    public static GlobalNetParams getTempInstance(Network network) {
        return new GlobalNetParams(network);
    }

    /**
     * Get a new instance of GNP and set the last instance to this new instance if the last decimal version and
     * privateKeyHeader is different than the current values.
     * @param decimalVersion - Defines the first letter of an address.
     * @param privateKeyHeader - Defines the private key format.
     * @return new or existing instance of GNP
     * @throws IllegalDecimalVersionException if decimalVersion or privateKeyHeader are not in [0, 255]
     */
    public static synchronized GlobalNetParams getAndSet(int decimalVersion, int privateKeyHeader) throws IllegalDecimalVersionException {
        if (instance == null) {
            instance = new GlobalNetParams(decimalVersion, privateKeyHeader);
        } else if (instance.addressHeader != decimalVersion && instance.dumpedPrivateKeyHeader != privateKeyHeader) {
            instance = new GlobalNetParams(decimalVersion, privateKeyHeader);
        }
        return instance;
    }

    /**
     * Get a new instance of GNP and set the last instance to this new instance if the last addressHeader,
     * privateKeyHeader, or p2shHeader is different the current values.
     * than this new decimal version.
     * @param decimalVersion - Defines the first letter of an address.
     * @param privateKeyHeader - Defines the private key format.
     * @param p2shHeader - Defines the address header for P2SH addresses.
     * @return new or existing instance of GNP.
     * @throws IllegalDecimalVersionException if decimalVersion or privateKeyHeader are not in [0, 255].
     */
    public static synchronized GlobalNetParams getAndSet(int decimalVersion, int privateKeyHeader, int p2shHeader) throws Exception {
        if (instance == null) {
            instance = new GlobalNetParams(decimalVersion, privateKeyHeader, p2shHeader);
        } else if (instance.addressHeader != decimalVersion && instance.dumpedPrivateKeyHeader != privateKeyHeader
                && instance.p2shHeader != p2shHeader) {
            instance = new GlobalNetParams(decimalVersion, privateKeyHeader, p2shHeader);
        }
        return instance;
    }

    /**
     * Get a new instance of GNP and set the last instance to this new network.
     * @param network - a pre-defined network to define this GNP instance.
     * @return new or existing instance of GNP.
     */
    public static synchronized GlobalNetParams getAndSet(Network network) {
        if (instance == null) {
            instance = new GlobalNetParams(network);
        } else if (instance.getNetwork() != network) {
            instance = new GlobalNetParams(network);
        }
        return instance;
    }

    /**
     * Gets the current instance of GNP. If no instance exists, an exception is thrown to prevent a wrongly chosen
     * network.
     * @return the current instance of GNP.
     */
    public static synchronized GlobalNetParams getInstance() throws Exception {
        if (instance == null) {
            throw new Exception("Instance does not exist!");
        }
        return instance;
    }

    private static void checkDecimal(int decimal) throws IllegalDecimalVersionException {
        if (decimal < 0 || decimal > 255) {
            throw new IllegalDecimalVersionException(decimal);
        }
    }

    /**
     * Returns a list of Prefix's matching this GNP based on the addressHeader.
     */
    public ArrayList<Prefix> getAssociatedPrefixes() {
        if (network == null) {
            return Prefix.getConfirmedAddressPrefixes(addressHeader);
        } else {
            return Prefix.getAddressPrefixes(network);
        }
    }

    /**
     * Returns the first Prefix that matches this addressHeader. Use getAssociatedPrefixes() for a complete list of
     * Prefix's that match this GNP addressHeader.
     */
    public Prefix getAssociatedPrefix() {
        if (network != null) {
            return Prefix.getFirstPrefixFrom(network);
        } else {
            return Prefix.getConfirmedAddressPrefixes(addressHeader).get(0);
        }
    }

    /**
     * If a Network is provided upon creation, that network is used. If there is a pair of addressHeader and
     * privateKeyHeaders provided, GNP will try to derive a network from that pair. If there is no existing Network
     * pair, network will be set to null.
     * @return the corresponding Network matching this GNP.
     */
    @Nullable
    public Network getNetwork() {
        return network;
    }

    @Override
    public String getPaymentProtocolId() {
        return "global";
    }

}
