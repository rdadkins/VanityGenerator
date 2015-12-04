package com.fatsoapps.vanitygenerator.core.network;

import com.fatsoapps.vanitygenerator.core.tools.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;

/**
 * Prefix is a compiled list of address headers based off of the Base58 wrapped in an enum.
 * See https://en.bitcoin.it/wiki/List_of_address_prefixes for more information.
 */
public enum Prefix {

    ONE(0),
    TWO(2, 3, 4, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158,
            159, 160, 161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174,
            175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189, 190,
            191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205, 206,
            207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 222,
            223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238,
            239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255),
    THREE(4, 5, 6, 7),
    FOUR(7, 8, 9),
    FIVE(9, 10, 11, 12),
    SIX(12, 13, 14),
    SEVEN(14, 15, 16, 17),
    EIGHT(17, 18, 19),
    NINE(19, 20, 21, 22),
    A(22, 23, 24),
    B(24, 25, 26, 27),
    C(27, 28, 29),
    D(29, 30, 31, 32),
    E(32, 33, 34),
    F(35, 36, 37),
    G(38, 39),
    H(39, 40, 41, 42),
    J(43, 44),
    K(44, 45, 46, 47),
    L(47, 48, 49),
    M(49, 50, 51, 52),
    N(52, 53, 54),
    P(54, 55, 56, 57),
    Q(1, 57, 58, 59),
    R(1, 59, 60, 61, 62),
    S(1, 62, 63, 64),
    T(1, 64, 65, 66, 67),
    U(1, 67, 68, 69),
    V(1, 69, 70, 71, 72),
    W(1, 72, 73, 74),
    X(1, 74, 75, 76, 77),
    Y(1, 78, 79),
    Z(1, 79, 80, 81, 82),
    a(1, 82, 83, 84),
    b(1, 84, 85, 86),
    c(1, 86, 87, 88, 89),
    d(1, 89, 90, 91),
    e(1, 91, 92, 93, 94),
    f(1, 94, 95, 96),
    g(1, 96, 97, 98, 99),
    h(1, 99, 100, 101),
    i(1, 101, 102, 103, 104),
    j(1, 104, 105, 106),
    k(1, 106, 107, 108, 109),
    m(1, 109, 110, 111),
    n(1, 111, 112, 113, 114),
    o(1, 2, 114, 115, 116),
    p(2, 116, 117, 118, 119),
    q(2, 119, 120, 121),
    r(2, 121, 122, 123, 124),
    s(2, 124, 125, 126),
    t(2, 126, 127, 128, 129),
    u(2, 129, 130, 131),
    v(2, 131, 132, 133, 134),
    w(2, 134, 135, 136),
    x(2, 136, 137, 138, 139),
    y(2, 139, 140, 141),
    z(2, 141, 142, 143, 144);

    private short[] versions;

    Prefix(int... intDecimals) {
        versions = new short[intDecimals.length];
        for (int i = 0; i < intDecimals.length; i++) {
            Utils.checkIfValidDecimal(intDecimals[i]);
            versions[i] = (short) intDecimals[i];
        }
    }

    /**
     * Used when configuring Query's to affix themselves to a certain character when using SearchPlacement.BEGINS.
     * @see com.fatsoapps.vanitygenerator.core.query.Query
     * @return the string defined by this Prefix.
     */
    public String toString() {
        String value;
        if (this == ONE) {
            value = "1";
        } else if (this == TWO) {
            value = "2";
        } else if (this == THREE) {
            value = "3";
        } else if (this == FOUR) {
            value = "4";
        } else if (this == FIVE) {
            value = "5";
        } else if (this == SIX) {
            value = "6";
        } else if (this == SEVEN) {
            value = "7";
        } else if (this == EIGHT) {
            value = "8";
        } else if (this == NINE) {
            value = "9";
        } else {
            value = name();
        }
        return value;
    }

    /**
     * Gets a list of Prefix's from an integer value.
     * @param value - a bounded value between [0, 255]
     * @return - a list of Prefix's that match this value.
     * @throws IllegalDecimalVersionException if value is out of range.
     */
    public static ArrayList<Prefix> getAddressPrefixes(int value) throws IllegalDecimalVersionException {
        Utils.checkIfValidDecimal(value);
        return getConfirmedAddressPrefixes(value);
    }

    /**
     * Returns a list of Prefix's from a GlobalNetParams input. If the network does not exist within the GNP, the address
     * header is used instead.
     * @param netParams - GlobalNetParams to get prefixes from
     * @return an ArrayList of Prefix's
     */
    public static ArrayList<Prefix> getAddressPrefixes(GlobalNetParams netParams) {
        if (netParams.getNetwork() == null) {
            return getConfirmedAddressPrefixes(netParams.getAddressHeader());
        } else {
            return getAddressPrefixes(netParams.getNetwork());
        }
    }

    /**
     * Returns a list of Prefix's that match a desired Network.
     * @param network - the Network to get Prefix's for.
     * @return a list of Prefix's matching the Network's addressHeader
     */
    public static ArrayList<Prefix> getAddressPrefixes(Network network) {
        return getConfirmedAddressPrefixes(network.getAddressHeader());
    }

    /**
     * This is protected because there needs to be certain logic applied before calling this method. Value MUST be in
     * range of [0, 255]. This also prevents other developers calling this method and wondering why no results are
     * returning.
     * @param value - the decimal value that is in range of [0, 255]
     * @return a list of Prefixes that contain value
     */
    protected static ArrayList<Prefix> getConfirmedAddressPrefixes(int value) {
        ArrayList<Prefix> values = new ArrayList<Prefix>();
        for (Prefix prefix: values()) {
            for (int decimal: prefix.versions) {
                if (value == decimal) {
                    values.add(prefix);
                }
            }
        }
        return values;
    }

    /**
     * Returns a Prefix from a character. If the character does not exist within the Prefix collection, null is returned.
     * @param c - character to match within list.
     * @return a Prefix that matches this character.
     */
    @Nullable
    public static Prefix fromCharacter(char c) {
        Prefix prefix = null;
        for (Prefix p: values()) {
            if (p.toString().charAt(0) == c) {
                prefix = p;
                break;
            }
        }
        return prefix;
    }

}
