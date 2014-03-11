package com.lovingheart.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.StoryContentActivity;
import com.lovingheart.app.adapter.UserStoryArrayAdapter;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/13.
 */
public class UserProfileStoriesFragment extends UserProfileFragment {

    private ExpandableListView userStoriesListView;

    private UserStoryArrayAdapter userStoryArrayAdapter;

    private List<ParseObject> userStoriesList;

    private View emptyView;

    public UserProfileStoriesFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userStoriesList = new ArrayList<ParseObject>();

        userStoryArrayAdapter = new UserStoryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userStoriesList);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryProfile(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null && parseUser.getObjectId() != null) {
                    queryStories(parseUser, new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (parseObjects != null && parseObjects.size() > 0) {

                                userStoriesList.clear();
                                userStoriesList.addAll(parseObjects);
                                userStoryArrayAdapter.notifyDataSetChanged();

                                emptyView.setVisibility(View.GONE);
                            } else {
                                emptyView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    Log.i(DailyKind.TAG, "Couldn't find the user profile : " + getUserId());
                }
                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.framgent_user_stories, container, false);

        userStoriesListView = (ExpandableListView)rootView.findViewById(R.id.me_stories_list_view);
        userStoriesListView.setExpand(true);
        userStoriesListView.setAdapter(userStoryArrayAdapter);
        userStoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject storyObject = userStoriesList.get(position);

                Intent storyIntent = new Intent(getActivity(), StoryContentActivity.class);
                storyIntent.putExtra("objectId", storyObject.getObjectId());

                getActivity().startActivity(storyIntent);
            }
        });

        emptyView = rootView.findViewById(R.id.user_profile_stories_empty_text_view);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        AnalyticsManager.getInstance().getGaTracker().send(
                MapBuilder.createAppView().set(
                        Fields.SCREEN_NAME, UserProfileStoriesFragment.class.getName())
                        .build());
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {

    }
}
