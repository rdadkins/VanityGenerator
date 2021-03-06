package co.bitsquared.vanitygenerator.android.search;

import android.os.Process;
import co.bitsquared.vanitygenerator.core.network.GlobalNetParams;
import co.bitsquared.vanitygenerator.core.query.RegexQuery;
import co.bitsquared.vanitygenerator.core.listeners.BaseSearchListener;
import co.bitsquared.vanitygenerator.core.search.Search;

/**
 * AndroidSearch is an extension of Search that sets the correct thread priority for searching. All other logic is held
 * within Search.
 * @deprecated Please use AndroidPoolSearch
 * @since v1.2.0
 */
@Deprecated
public class AndroidSearch extends Search {

    public AndroidSearch(BaseSearchListener listener, GlobalNetParams netParams, RegexQuery... queries) {
        super(listener, netParams, queries);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        super.run();
    }

}
