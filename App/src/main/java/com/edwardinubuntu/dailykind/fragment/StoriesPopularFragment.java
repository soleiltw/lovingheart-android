package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.util.Log;
import com.edwardinubuntu.dailykind.DailyKind;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class StoriesPopularFragment extends StoriesFeedsFragment {

    public static StoriesPopularFragment newInstance(int sectionNumber) {
        StoriesPopularFragment fragment = new StoriesPopularFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    protected void loadStories(boolean more) {

        Log.d(DailyKind.TAG, "StoriesPopularFragment loadStories more: "+more);

        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("viewCount");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");
        parseQuery.setLimit(10);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        if (more) {
            ParseQuery.getQuery("Story").countInBackground(new CountCallback() {
                @Override
                public void done(int totalCount, ParseException e) {
                    if (totalCount > userActivities.size()) {
                        parseQuery.setSkip(userActivities.size());
                        queryToCallBack(parseQuery);
                    } else {
                        Log.d(DailyKind.TAG, "End of query.");
                    }
                }
            });

        } else {
            userActivities.clear();
            queryToCallBack(parseQuery);
        }

    }
}
