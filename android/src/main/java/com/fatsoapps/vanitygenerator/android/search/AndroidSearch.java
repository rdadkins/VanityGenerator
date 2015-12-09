package com.fatsoapps.vanitygenerator.android.search;

import android.os.Process;
import com.fatsoapps.vanitygenerator.core.network.GlobalNetParams;
import com.fatsoapps.vanitygenerator.core.query.RegexQuery;
import com.fatsoapps.vanitygenerator.core.search.BaseSearchListener;
import com.fatsoapps.vanitygenerator.core.search.Search;

/**
 * AndroidSearch is an extension of Search that sets the correct thread priority for searching. All other logic is held
 * within Search.
 */
public class AndroidSearch extends Search {

    public <T extends RegexQuery> AndroidSearch(BaseSearchListener listener, GlobalNetParams netParams, T... queries) {
        super(listener, netParams, queries);
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        super.run();
    }
}
