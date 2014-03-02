package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.lovingheart.app.DailyKind;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
    protected void loadStories(boolean more) {
        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");
        parseQuery.setLimit(10);
        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        if (more) {

            ParseQuery storyCountQuery = ParseQuery.getQuery("Story");
            storyCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            storyCountQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
            storyCountQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
            storyCountQuery.countInBackground(new CountCallback() {
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