package co.bitsquared.vanitygenerator.examples;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.network.Network;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.search.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.PoolSearch;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * This is an example of how to use multiple BaseSearchListeners in PoolSearch
 */
public class MultiListenerExample {

    public static void main(String[] args) {
        new MultiListenerExample().startExample();
    }

    public void startExample() {
        QueryPool pool = QueryPool.getInstance(Network.BITCOIN, true);
        RegexQuery query = new RegexQuery(Pattern.compile(".*test.*"), true);
        pool.addQuery(query);
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            service.execute(new PoolSearch(getListener(i), pool , Network.BITCOIN.toGlobalNetParams()));
        }
        service.shutdown();
    }

    public BaseSearchListener getListener(final int id) {
        System.out.println("Creating listener: " + id);
        return new BaseSearchListener() {

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
                System.out.printf("%d:[updateBurstGenerated] %d%n", id, burstGenerated);
            }

            public void onTaskCompleted(long totalGenerated, long speedPerSecond) {
                System.out.printf("%d:[onTaskComplete] %d%n", id, totalGenerated);
            }
        };
    }

}
