package com.fatsoapps.vanitygenerator.examples;

import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.Query;
import com.fatsoapps.vanitygenerator.core.query.QueryPool;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.PoolSearch;
import com.fatsoapps.vanitygenerator.networks.Network;
import org.bitcoinj.core.ECKey;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An example of a multi-threaded approach of searching. Each search thread has a different update amount to show an
 * example of updating each thread at a different interval as opposed to having each thread update nearly at the same
 * time.
 */
public class PoolSearchExample implements BaseSearchListener {

    private static final GlobalNetParams netParams = GlobalNetParams.getAndSet(Network.BITCOIN);

    public static void main(String[] args) {
        new PoolSearchExample().startExample();
    }

    public void startExample() {
        Query easyQuery = new Query("FUN", true, false, true, netParams.getNetwork());
        Query hardQuery = new Query("FUN", true, true, false, netParams.getNetwork());
        System.out.println("Odds: 1/" + easyQuery.getOdds());
        System.out.println("Odds: 1/" + hardQuery.getOdds());
        QueryPool pool = QueryPool.getInstance(netParams.getNetwork(), false);
        pool.addQuery(easyQuery);
        pool.addQuery(hardQuery);
        ExecutorService service = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 4; i++) {
            PoolSearch search = new PoolSearch(this, pool, netParams);
            search.setUpdateAmount(new Random().nextInt(5000) + 1000); // We can see threads report back at different times.
            service.execute(search);
        }
        service.shutdown();
    }

    public void onAddressFound(ECKey key, long amountGenerated, boolean isCompressed) {
        if (!isCompressed) {
            key = key.decompress();
        }
        System.out.printf("%s %s found after %d attempts.%n", isCompressed ? "[Compressed]" : "[Uncompressed]", key.toAddress(netParams), amountGenerated);
    }

    public void updateBurstGenerated(long totalGenerated, long burstGenerated) {
        System.out.printf("%d generated since last update, %d total generated.%n", burstGenerated, totalGenerated);
    }

    public void onTaskCompleted(long totalGenerated) {
        System.out.printf("Task completed with %d addresses generated.%n", totalGenerated);
    }

}
