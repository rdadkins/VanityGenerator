package com.fatsoapps.vanitygenerator.examples;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.Query;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.Search;
import com.fatsoapps.vanitygenerator.core.network.Network;
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
        RegexQuery regexQuery = new RegexQuery(pattern, true, false);
        RegexQuery secondRegexQuery = new RegexQuery(secondPattern, true, false);
        Query normalQuery = new Query("FUN", true, false, true, netParams.getNetwork());
        Search search = new Search(this, netParams, regexQuery, normalQuery, secondRegexQuery);
        new Thread(search).start();
    }

    public void onAddressFound(ECKey key, long amountGenerated, long speedPerSecond, boolean isCompressed) {
        if (!isCompressed) {
            key = key.decompress();
        }
        System.out.println(key.toAddress(netParams) + " found after " + amountGenerated + " attempts.");
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speedPerSecond) {
        System.out.printf("%d : %d. Speed: %d/s%n", burstGenerated, totalGenerated, speedPerSecond);
    }

    public void onTaskCompleted(long totalGenerated, long speedPerSecond) {
        System.out.printf("Total: %d. Speed: %d/s%n", totalGenerated, speedPerSecond);
    }
}
