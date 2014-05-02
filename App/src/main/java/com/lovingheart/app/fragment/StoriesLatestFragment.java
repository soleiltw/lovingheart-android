package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.object.parse.Flag;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;

/**
 * Created by edward_chiang on 2014/1/10.
 */
public class StoriesLatestFragment extends StoriesFeedsFragment {

    public static StoriesLatestFragment newInstance(int sectionNumber) {
        StoriesLatestFragment fragment = new StoriesLatestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    protected void loadStories(final boolean more) {
        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");

        ParseQuery<Flag> flagQuery = ParseQuery.getQuery(Flag.class);
        flagQuery.whereEqualTo("Object", "Story");
        flagQuery.whereEqualTo("Status", "Close");

        parseQuery.whereDoesNotMatchKeyInQuery("objectId", "ObjID", flagQuery);
        parseQuery.whereNotContainedIn("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
        parseQuery.setLimit(10);
        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        if (more) {

            ParseQuery storyCountQuery = ParseQuery.getQuery("Story");
            storyCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            storyCountQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
            storyCountQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
            parseQuery.whereNotContainedIn("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
            storyCountQuery.countInBackground(new CountCallback() {
                @Override
                public void done(int totalCount, ParseException e) {
                    if (totalCount > userActivities.size()) {
                        parseQuery.setSkip(userActivities.size());
                        queryToCallBack(parseQuery, more);
                    } else {
                        Log.d(DailyKind.TAG, "End of query.");
                    }
                }
            });

        } else {
            userActivities.clear();
            queryToCallBack(parseQuery, more);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "Stories From Latest");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "Stories From Latest");
        gaParams.put(Fields.EVENT_LABEL, "All");
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }
}
