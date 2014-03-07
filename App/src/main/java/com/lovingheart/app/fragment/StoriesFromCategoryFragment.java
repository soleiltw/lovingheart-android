package com.lovingheart.app.fragment;

import android.os.Bundle;
import android.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by edward_chiang on 2014/3/1.
 */
public class StoriesFromCategoryFragment extends StoriesFeedsFragment {

    private ParseObject ideaObject;

    public static StoriesFromCategoryFragment newInstance(int sectionNumber) {
        StoriesFromCategoryFragment fragment = new StoriesFromCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void loadStories(final boolean more) {
        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");
        parseQuery.setLimit(DailyKind.PARSE_QUERY_LIMIT);
        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        parseQuery.whereEqualTo("ideaPointer", ideaObject);
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

    public ParseObject getIdeaObject() {
        return ideaObject;
    }

    public void setIdeaObject(ParseObject ideaObject) {
        this.ideaObject = ideaObject;
    }

    @Override
    public void onStart() {
        super.onStart();
        AnalyticsManager.getInstance().getGaTracker().send(
                MapBuilder.createAppView().set(Fields.SCREEN_NAME, StoriesFromCategoryFragment.class.getName()).build());
    }
}
