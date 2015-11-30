package com.fatsoapps.vanitygenerator.core.query;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import org.bitcoinj.core.ECKey;

import java.util.regex.Pattern;

/**
 * RegexQuery is the base Query type that is used to search for strings in ECKey addresses from regular expressions.
 * This class contains the base elements needed to search for addresses within a Search thread or PoolSearch thread.
 * This class is not capable of easily enforcing Prefix or Networks and should be used by those who know to use regular
 * expressions. To enforce Prefix / Network restrictions, you should use Query which is a more restricted Query type
 * that extends this class.
 * @see org.bitcoinj.core.ECKey
 * @see org.bitcoinj.core.Address
 * @see com.fatsoapps.vanitygenerator.core.query.Query
 */
public class RegexQuery {

    protected Pattern pattern;
    protected boolean compressed;
    protected boolean findUnlimited;

    /**
     * Default constructor for Query.super() calls.
     * @param findUnlimited - specifies whether to delete this Query from a collection when this Query is found.
     */
    protected RegexQuery(boolean findUnlimited) {
        this.findUnlimited = findUnlimited;
    }

    public RegexQuery(Pattern pattern, boolean compressed) {
        this(pattern, compressed, false);
    }

    public RegexQuery(Pattern pattern, boolean compressed, boolean findUnlimited) {
        this.pattern = pattern;
        this.compressed = compressed;
        this.findUnlimited = findUnlimited;
    }

    public boolean isFindUnlimited() {
        return findUnlimited;
    }

    public void setFindUnlimited(boolean findUnlimited) {
        this.findUnlimited = findUnlimited;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompresion(boolean compressed) {
        this.compressed = compressed;
    }

    public boolean matches(ECKey key, GlobalNetParams netParams) {
        if (!compressed) {
            key = key.decompress();
        }
        return matches(key.toAddress(netParams).toString());
    }

    public boolean matches(String input) {
        return pattern.matcher(input).find();
    }

    public int hashCode() {
        int hash = 17;
        hash += 23 * (hash + (findUnlimited ? 17 : 7));
        hash += 23 * (hash + (compressed ? 17 : 7));
        hash += 23 * (hash + (pattern != null ? pattern.hashCode() : 7));
        return hash;
    }

    public boolean equals(RegexQuery other) {
        return this.hashCode() == other.hashCode();
    }

}
