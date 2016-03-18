package co.bitsquared.vanitygenerator.examples;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.Base58FormatException;
import co.bitsquared.vanitygenerator.core.query.Query;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.search.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.Search;
import co.bitsquared.vanitygenerator.core.network.Network;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

/**
 * This is a basic example of how to use Search and the BaseSearchListener methods. You can create multiple Search
 * threads, but each search thread will be looking for each Query which means once X-threads are done executing, you
 * will end up with X amount of Query's found. This means that Search is a single threaded approach. If you plan on
 * doing a multithreaded approach see {@link PoolSearchExample}.
 */
public class SearchExample implements BaseSearchListener {

    private final static GlobalNetParams netParams = GlobalNetParams.get(Network.BITCOIN);

    public static void main(String[] args) throws Base58FormatException {
        new SearchExample().startExample();
    }

    public void startExample() throws Base58FormatException {
        Query testQuery = new Query.QueryBuilder("test").compressed(true).begins(false).matchCase(false).build();
        Query quickQuery = new Query.QueryBuilder("").compressed(true).begins(true).matchCase(true).build();
        Search singleSearchThread = new Search(this, netParams, testQuery, quickQuery);
        singleSearchThread.run();
    }

    public void onAddressFound(ECKey key, GlobalNetParams netParams, long amountGenerated, long speed, RegexQuery query) {
        if (!query.isCompressed()) {
            key = key.decompress();
        }
        if (query.isP2SH()) {
            System.out.println(Address.fromP2SHHash(netParams, key.getPubKeyHash()) + " found after " + amountGenerated + " attempts.");
        } else {
            System.out.println(key.toAddress(netParams) + " found after " + amountGenerated + " attempts.");
        }
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speed) {
        System.out.printf("%d generated since last update. %d have been generated.%n", burstGenerated, totalGenerated);
    }

    public void onTaskCompleted(long totalGenerated, long speed) {
        System.out.printf("Task has finished. %d have been generated.%n", totalGenerated);
    }
}
