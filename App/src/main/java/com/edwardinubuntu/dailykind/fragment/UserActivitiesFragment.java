package com.edwardinubuntu.dailykind.fragment;

import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.adapter.UserActivitiesAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_user_activities, container, false);

        ListView userActivitiesListView = (ListView)rootView.findViewById(R.id.user_activities_list_view);
        userActivitiesListView.setAdapter(userActivitiesAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_activities, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                loadStories();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadStories();
    }

    private void loadStories() {
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
