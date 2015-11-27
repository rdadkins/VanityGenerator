package com.fatsoapps.vanitygenerator.examples;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.Query;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.Search;
import com.fatsoapps.vanitygenerator.networks.Network;
import org.bitcoinj.core.ECKey;

/**
 * This is a basic example of how to use Search and the BaseSearchListener methods. You can create multiple Search
 * threads, but each search thread will be looking for each Query which means once X-threads are done executing, you
 * will end up with X amount of Query's found. This means that Search is a single threaded approach. If you plan on
 * doing a multithreaded approach see {@link com.fatsoapps.vanitygenerator.examples.PoolSearchExample}.
 */
public class SearchExample implements BaseSearchListener {

    private final static GlobalNetParams netParams = GlobalNetParams.getAndSet(Network.BITCOIN);

    public static void main(String[] args) {
        new SearchExample().startExample();
    }

    public void startExample() {
        Query testQuery = new Query("test", false, false, true, netParams.getNetwork());
        Query quickQuery = new Query("1", false, false, true, netParams.getNetwork());
        Search singleSearchThread = new Search(this, netParams, testQuery, quickQuery);
        singleSearchThread.run();
    }

    public void onAddressFound(ECKey key, long amountGenerated, long speed, boolean compressed) {
        key = compressed ? key : key.decompress();
        System.out.printf("%d addresses generated to find %s%n", amountGenerated, key.toAddress(netParams));
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speed) {
        System.out.printf("%d generated since last update. %d have been generated.%n", burstGenerated, totalGenerated);
    }

    public void onTaskCompleted(long totalGenerated, long speed) {
        System.out.printf("Task has finished. %d have been generated.%n", totalGenerated);
    }
}
