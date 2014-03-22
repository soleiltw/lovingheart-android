package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.StoryContentActivity;
import com.lovingheart.app.adapter.StoryAnonymousAdapter;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;

/**
 * Created by edward_chiang on 2014/3/22.
 */
public class StoriesAnonymousFragment extends StoriesFeedsFragment {

    public static StoriesAnonymousFragment newInstance(int sectionNumber) {
        StoriesAnonymousFragment fragment = new StoriesAnonymousFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        storyArrayAdapter = new StoryAnonymousAdapter(getActivity(), android.R.layout.simple_list_item_1, userActivities);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        ListView userActivitiesListView = (ListView)rootView.findViewById(R.id.user_activities_list_view);
        userActivitiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent storyContentIntent = new Intent(getActivity(), StoryContentActivity.class);
                ParseObject activity = userActivities.get(position);
                storyContentIntent.putExtra("objectId", activity.getObjectId());
                storyContentIntent.putStringArrayListExtra("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
                startActivity(storyContentIntent);
            }
        });
        return rootView;
    }

    protected void loadStories(final boolean more) {

        Log.d(DailyKind.TAG, "StoriesPopularFragment loadStories more: " + more);

        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");
        parseQuery.whereContainedIn("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        parseQuery.setLimit(10);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        parseQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        if (more) {
            ParseQuery queryStoriesCount = ParseQuery.getQuery("Story");
            queryStoriesCount.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
            queryStoriesCount.whereNotEqualTo("status", DailyKind.getAnonymousStoriesStatusList(getActivity()));
            queryStoriesCount.countInBackground(new CountCallback() {
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
        gaParams.put(Fields.SCREEN_NAME, "Stories From Anonymous");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "Stories From Anonymous");
        gaParams.put(Fields.EVENT_LABEL, "All");
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }
}
