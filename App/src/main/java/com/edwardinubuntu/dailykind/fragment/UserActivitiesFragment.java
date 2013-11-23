package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.UserActivitiesAdapter;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class UserActivitiesFragment extends PlaceholderFragment {

    private List<ParseObject> userActivities;

    private UserActivitiesAdapter userActivitiesAdapter;

    public static UserActivitiesFragment newInstance(int sectionNumber) {
        UserActivitiesFragment fragment = new UserActivitiesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userActivities = new ArrayList<ParseObject>();

        userActivitiesAdapter = new UserActivitiesAdapter(getActivity(), android.R.layout.simple_list_item_1, userActivities);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_activities, container, false);

        ListView userActivitiesListView = (ListView)rootView.findViewById(R.id.user_activities_list_view);
        userActivitiesListView.setAdapter(userActivitiesAdapter);

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
                userActivities.clear();
                userActivities.addAll(parseObjects);
                userActivitiesAdapter.notifyDataSetChanged();
            }
        });
    }
}
