package co.bitsquared.vanitygenerator.core.network;

import co.bitsquared.vanitygenerator.core.exceptions.P2SHNotInitializedException;
import co.bitsquared.vanitygenerator.core.tools.Utils;

import javax.annotation.Nullable;

/**
 * Network is a collection of defined crypto currencies with their pre-configured address header, private key
 * headers, and an optional P2SH header.
 */
public enum Network {

    /**
     * Name: Bitcoin
     * Public Key Header: 0
     * Private Key Header: 128
     * P2SH Header: 5
     */
    BITCOIN(0, 5, 128),

    /**
     * Name: Bitcoin Test
     * Public Key Header: 111
     * Private Key Header: 239
     * P2SH Header: 196
     */
    BITCOIN_TEST(111, 196, 239),

    /**
     * Name: Litecoin
     * Public Key Header: 48
     * Private Key Header: 176
     * P2SH Header: 5
     */
    LITECOIN(48, 5, 176),

    /**
     * Name: Dashcoin (formally Darkcoin)
     * Public Key Header: 76
     * Private Key Header: 204
     * P2SH Header: 16
     */
    DASHCOIN(76, 16, 204),

    /**
     * Name: Dogecoin
     * Public Key Header: 30
     * Private Key Header: 158
     * P2SH Header: 22
     */
    DOGECOIN(30, 22, 158),

    /**
     * Name: BlackCoin
     * Public Key Header: 25
     * Private Key Header: 153
     * P2SH Header: 85
     */
    BLACKCOIN(25, 85, 153),

    /**
     * Name: Mastercoin
     * Public Key Header: 0
     * Private Key Header: 128
     * P2SH Header: 5
     */
    MASTERCOIN(0, 5, 128),

    /**
     * Name: MazaCoin
     * Public Key Header: 50
     * Private Key Header: 224
     * P2SH Header: 9
     */
    MAZACOIN(50, 9, 224);

    private int addressHeader;
    private int privateKeyHeader;
    private int p2shHeader = -1;

    Network(int addressHeader, int p2shHeader, int privateKeyHeader) {
        Utils.checkIfValidDecimal(addressHeader);
        Utils.checkIfValidDecimal(p2shHeader);
        Utils.checkIfValidDecimal(privateKeyHeader);
        this.addressHeader = addressHeader;
        this.privateKeyHeader = privateKeyHeader;
        this.p2shHeader = p2shHeader;
    }

    /**
     * Returns the public key header for this network. This is the header that is used for public key address formatting.
     */
    public int getAddressHeader() {
        return addressHeader;
    }

    /**
     * Returns the private key header for this network. This is the header that is used for private key address formatting.
     */
    public int getPrivateKeyHeader() {
        return privateKeyHeader;
    }

    /**
     * Returns the P2SH header for this network. This is the header that is used for P2SH address formatting.
     * @throws P2SHNotInitializedException if this network does not define a P2SH Header
     * @return the P2SH header if it exists.
     */
    public int getP2SHHeader() {
        if (p2shHeader == -1) {
            throw new P2SHNotInitializedException(name());
        }
        return p2shHeader;
    }

    /**
     * Returns the first network whose address header matches addressHeader
     * @deprecated no use of this method since many different coins share the same headers
     */
    @Deprecated
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

    /**
     * Derives a Network from a public key header, private key header, and a P2SH header. Returns null if the pair does
     * not exist.
     */
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

    /**
     * Creates an instance of GlobalNetParams out of this network.
     */
    public GlobalNetParams toGlobalNetParams() {
        return new GlobalNetParams(this);
    }

}
