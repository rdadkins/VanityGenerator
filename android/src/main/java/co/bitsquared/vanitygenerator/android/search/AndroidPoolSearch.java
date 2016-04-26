package co.bitsquared.vanitygenerator.android.search;

import android.os.Process;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.QueryPool;
import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.PoolSearch;

/**
 * AndroidPoolSearch is an extension of PoolSearch built for Android. The main purpose this class serves is setting
 * the proper thread priority for mobile devices. All other logic is held within PoolSearch.
 */
public class AndroidPoolSearch extends PoolSearch {

    private AndroidPoolSearch (AndroidPoolSearchBuilder builder) {
        super(builder);
    }

    /**
     * @deprecated since v1.3.0 - use AndroidPoolSearchBuilder
     */
    @Deprecated
    public AndroidPoolSearch(BaseSearchListener listener, QueryPool pool, GlobalNetParams netParams) {
        super(listener, pool, netParams);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        super.run();
    }

    public static class AndroidPoolSearchBuilder extends PoolSearchBuilder {

        /**
         * Create an AndroidPoolSearchBuilder from a QueryPool.
         *
         * @param pool a nonnull QueryPool.
         * @throws NullPointerException if pool is null.
         */
        public AndroidPoolSearchBuilder(QueryPool pool) {
            super(pool);
        }

        @Override
        public AndroidPoolSearch build() {
            return new AndroidPoolSearch(this);
        }

    }

}
