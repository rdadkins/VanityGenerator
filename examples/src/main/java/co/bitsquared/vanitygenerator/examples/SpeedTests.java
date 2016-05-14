package co.bitsquared.vanitygenerator.examples;

import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.network.Network;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.PoolSearch;
import org.bitcoinj.core.ECKey;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class SpeedTests {

    public static void main(String[] args) {
        new SpeedTests().maxSpeed(4);
    }

    public void maxSpeed(int threads) {
        Network network = Network.BITCOIN;
        RegexQuery query = new RegexQuery(Pattern.compile(".*Bitcoin.*"), true, false, false);
        QueryPool pool = QueryPool.getInstance(network, false);
        MaxSpeed listener = new MaxSpeed();
        pool.addQuery(query);
        ExecutorService service = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            service.execute(new PoolSearch(listener, pool, network.toGlobalNetParams()).setUpdateAmount(5000));
        }
        try {
            service.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        service.shutdownNow();
        System.out.println("Max Speed: " + listener.maxSearchSpeed);
    }

    public static class MaxSpeed implements BaseSearchListener {

        public long maxSearchSpeed = 0;

        public void onAddressFound(ECKey key, GlobalNetParams netParams, long amountGenerated, long speedPerSecond, RegexQuery query) {
            setSpeed(speedPerSecond);
        }

        public void updateBurstGenerated(long totalGenerated, long burstGenerated, long speedPerSecond) {
            setSpeed(speedPerSecond);
        }

        public void onTaskCompleted(long totalGenerated, long speedPerSecond) {
            setSpeed(speedPerSecond);
        }

        public void setSpeed(long speedPerSecond) {
            System.out.println("Search Speed: " + speedPerSecond);
            if (speedPerSecond > maxSearchSpeed) {
                maxSearchSpeed = speedPerSecond;
            }
        }

    }

}
