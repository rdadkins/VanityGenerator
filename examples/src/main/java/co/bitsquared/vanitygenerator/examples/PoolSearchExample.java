package co.bitsquared.vanitygenerator.examples;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.Base58FormatException;
import co.bitsquared.vanitygenerator.core.query.Query;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.search.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.PoolSearch;
import co.bitsquared.vanitygenerator.core.network.Network;
import org.bitcoinj.core.Address;
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

    private static final GlobalNetParams netParams = GlobalNetParams.get(Network.BITCOIN);

    public static void main(String[] args) throws Base58FormatException {
        new PoolSearchExample().startExample();
    }

    public void startExample() throws Base58FormatException {
        Query easyQuery = new Query.QueryBuilder("FUN").compressed(true).begins(false).matchCase(true).findUnlimited(false).build();
        Query hardQuery = new Query.QueryBuilder("FUNN").
                compressed(true).
                begins(false).
                matchCase(false).
                findUnlimited(false).
                searchForP2SH(true).
                targetNetwork(GlobalNetParams.get(Network.LITECOIN)).build();
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

    public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speed) {
        System.out.printf("%d generated since last update, %d total generated. Speed: %d.%n", burstGenerated, totalGenerated, speed);
    }

    public void onTaskCompleted(long totalGenerated, long speed) {
        System.out.printf("Task completed with %d addresses generated. Average speed: %d.%n", totalGenerated, speed);
    }

}
