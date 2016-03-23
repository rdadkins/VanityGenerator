package co.bitsquared.vanitygenerator.core.query;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.network.Network;
import co.bitsquared.vanitygenerator.core.network.Prefix;
import org.bitcoinj.core.ECKey;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * NetworkQuery is an extension of RegexQuery which allows a user to define a Query to match on a specific Network.
 * Usage would be when there are multiple Query's but all containing a different Network such that a user wants
 * a match on Bitcoin and wants a match on Litecoin but does not want to create separate search threads. Each NetworkQuery
 * contains its own Pattern and GlobalNetParams when it comes to matches().
 */
@Deprecated /** Use Query / RegexQuery */
public class NetworkQuery extends RegexQuery {

    public NetworkQuery(Pattern pattern, boolean compressed, Network network) {
        this(pattern, compressed, false, network);
    }

    public NetworkQuery(Pattern pattern, boolean compressed, boolean findUnlimited, Network network) {
        super(pattern, compressed, findUnlimited, false);
        updateNetwork(network);
    }

    public void updateNetwork(Network network) {
        updateNetParams(network.toGlobalNetParams());
    }

    @Override
    public void updateNetParams(GlobalNetParams netParams) {
        this.netParams = netParams;
        updatePattern();
    }

    @Override
    public GlobalNetParams getNetworkParameters(GlobalNetParams netParams) {
        return this.netParams;
    }

    @Override
    public boolean matches(ECKey key, GlobalNetParams netParams) {
        return super.matches(key, this.netParams);
    }

    @Override
    public Pattern getPattern() {
        return pattern;
    }

    private void updatePattern() {
        ArrayList<Prefix> prefixes = Prefix.getAddressPrefixes(netParams);
        StringBuilder prefixBuilder = new StringBuilder("(");
        for (Prefix prefix: prefixes) {
            prefixBuilder.append(prefix.toString());
            if (prefix != prefixes.get(prefixes.size() - 1)) {
                prefixBuilder.append("|");
            }
        }
        prefixBuilder.append(")");
        pattern = Pattern.compile("^" + prefixBuilder.toString() + pattern.toString().replace("^",""));
    }

}
