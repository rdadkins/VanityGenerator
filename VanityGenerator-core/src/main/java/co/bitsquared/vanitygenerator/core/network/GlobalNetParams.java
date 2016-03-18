package co.bitsquared.vanitygenerator.core.network;

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
 * <b>NOTE</b>: This class is similar to org.bitcoinj.core.Context in bitcoinj 0.13+ but Context will not be used since this
 * class was under development before Context's debut, and it is not readily available for use.
 * @see org.bitcoinj.core.ECKey
 * @see org.bitcoinj.core.Address
 */
public class GlobalNetParams extends NetworkParameters {

    private static GlobalNetParams instance = null;
    private Network network;

    public GlobalNetParams(Network network) {
        this.addressHeader = network.getAddressHeader();
        this.dumpedPrivateKeyHeader = network.getPrivateKeyHeader();
        try {
            this.p2shHeader = network.getP2shHeader();
            acceptableAddressCodes = new int[] {addressHeader, dumpedPrivateKeyHeader, p2shHeader};
        } catch (Exception e) {
            acceptableAddressCodes = new int[] {addressHeader, dumpedPrivateKeyHeader};
        }
        this.network = network;
    }

    public GlobalNetParams(int addressHeader, int privateKeyHeader) {
        checkDecimal(addressHeader);
        checkDecimal(privateKeyHeader);
        this.addressHeader = addressHeader;
        this.dumpedPrivateKeyHeader = privateKeyHeader;
        acceptableAddressCodes = new int[] {addressHeader, privateKeyHeader};
        network = Network.deriveFrom(addressHeader, privateKeyHeader);
    }

    public GlobalNetParams(int addressHeader, int dumpedPrivateKeyHeader, int p2shHeader) {
        checkDecimal(addressHeader);
        checkDecimal(dumpedPrivateKeyHeader);
        checkDecimal(p2shHeader);
        this.addressHeader = addressHeader;
        this.dumpedPrivateKeyHeader = dumpedPrivateKeyHeader;
        this.p2shHeader = p2shHeader;
        acceptableAddressCodes = new int[] {this.addressHeader, p2shHeader};
        network = Network.deriveFrom(addressHeader, dumpedPrivateKeyHeader, p2shHeader);
    }

    /**
     * Creates a shared instance from a Network.
     */
    public static GlobalNetParams get(Network network) {
        if (instance == null || instance.getNetwork() != network) {
            instance = new GlobalNetParams(network);
        }
        return instance;
    }

    /**
     * Creates a shared instance from a custom set of headers.
     */
    public static GlobalNetParams get(int addressHeader, int privateKeyHeader, int p2shHeader) {
        if (instance == null) {
            instance = new GlobalNetParams(addressHeader, privateKeyHeader, p2shHeader);
        } else if (instance.addressHeader != addressHeader || instance.dumpedPrivateKeyHeader != privateKeyHeader
                || instance.p2shHeader != p2shHeader) {
            instance = new GlobalNetParams(addressHeader, privateKeyHeader, p2shHeader);
        }
        return instance;
    }

    /**
     * Gets the latest shared instance. Instance will be null if it was not instantiated via get().
     */
    @Nullable
    public static synchronized GlobalNetParams getInstance() {
        return instance;
    }

    private static void checkDecimal(int decimal) {
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
