package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.FeedsActivitiesAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class FeedsActivitiesFragment extends PlaceholderFragment {

    private List<ParseObject> feedsActivities;

    private FeedsActivitiesAdapter feedsActivitiesAdapter;

    public static FeedsActivitiesFragment newInstance(int sectionNumber) {
        FeedsActivitiesFragment fragment = new FeedsActivitiesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        feedsActivities = new ArrayList<ParseObject>();

        feedsActivitiesAdapter = new FeedsActivitiesAdapter(getActivity(), android.R.layout.simple_list_item_1, feedsActivities);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_feeds_activities, container, false);

        ListView feedsActivitiesListView = (ListView)rootView.findViewById(R.id.feeds_activities_list_view);
        feedsActivitiesListView.setAdapter(feedsActivitiesAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                feedsActivities.clear();
                feedsActivities.addAll(parseObjects);
                feedsActivitiesAdapter.notifyDataSetChanged();
            }
        });
    }
}
