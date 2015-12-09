package com.fatsoapps.vanitygenerator.examples;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.network.Network;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.PoolSearch;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;

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
        PoolSearch search = new PoolSearch(getListener(0), pool, GlobalNetParams.get(pool.getNetwork()));
        search.registerListener(getListener(1));
        new Thread(search).start();
    }

    public BaseSearchListener getListener(final int id) {
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
