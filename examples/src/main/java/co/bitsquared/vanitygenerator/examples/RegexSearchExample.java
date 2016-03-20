package co.bitsquared.vanitygenerator.examples;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.Search;
import co.bitsquared.vanitygenerator.core.network.Network;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

import java.util.regex.Pattern;

/**
 * This is an example of searching for RegexQuery's in a single Search thread. A regular Query is included as well.
 */
public class RegexSearchExample implements BaseSearchListener {

    private static final GlobalNetParams netParams = GlobalNetParams.get(Network.BITCOIN);

    public static void main(String[] args) {
        new RegexSearchExample().startExample();
    }

    public void startExample() {
        Pattern pattern = Pattern.compile("^.*fun.*$"); // Contains fun
        Pattern secondPattern = Pattern.compile("^(?i)1(fun|test|hi|fatso|guy).*$"); // starts with any of these words where case doesn't matter
        Pattern endsPattern = Pattern.compile("^.*YES$"); // ends with YES
        RegexQuery regexQuery = new RegexQuery(pattern, true, false, false);
        RegexQuery secondRegexQuery = new RegexQuery(secondPattern, true, false, false);
        RegexQuery thirdRegexQuery = new RegexQuery(endsPattern, true, false, true); // P2SH Address
        Search search = new Search(this, netParams, regexQuery, secondRegexQuery, thirdRegexQuery);
        new Thread(search).start();
    }

    public void onAddressFound(ECKey key, GlobalNetParams netParams, long amountGenerated, long speedPerSecond, RegexQuery query) {
        if (!query.isCompressed()) {
            key = key.decompress();
        }
        if (query.isP2SH()) {
            System.out.println(Address.fromP2SHHash(netParams, key.getPubKeyHash()) + " found after " + amountGenerated + " attempts.");
        } else {
            System.out.println(key.toAddress(netParams) + " found after " + amountGenerated + " attempts.");
        }
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speedPerSecond) {
        System.out.printf("%d : %d. Speed: %d/s%n", burstGenerated, totalGenerated, speedPerSecond);
    }

    public void onTaskCompleted(long totalGenerated, long speedPerSecond) {
        System.out.printf("Total: %d. Speed: %d/s%n", totalGenerated, speedPerSecond);
    }
}
