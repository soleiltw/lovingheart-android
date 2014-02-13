package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.StoryContentActivity;
import com.edwardinubuntu.dailykind.adapter.UserStoryArrayAdapter;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2014/2/13.
 */
public class UserProfileStoriesFragment extends PlaceholderFragment {

    private ListView userStoriesListView;

    private UserStoryArrayAdapter userStoryArrayAdapter;

    private List<ParseObject> userStoriesList;

    private String userId;

    public UserProfileStoriesFragment() {
        setupUserId();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userStoriesList = new ArrayList<ParseObject>();

        userStoryArrayAdapter = new UserStoryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userStoriesList);

        setupUserId();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.framgent_user_stories, container, false);

        userStoriesListView = (ListView)rootView.findViewById(R.id.me_stories_list_view);
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

        return rootView;
    }


    protected void queryStories(ParseUser parseUser) {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", parseUser);
        storyQuery.orderByDescending("createdAt");
        storyQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        storyQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        storyQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null && parseObjects.size() > 0) {

                    userStoriesList.clear();
                    userStoriesList.addAll(parseObjects);
                    userStoryArrayAdapter.notifyDataSetChanged();

                    if (getActivity()!=null) {
                        getActivity().findViewById(R.id.user_profile_stories_empty_text_view).setVisibility(View.GONE);
                    }
                } else {
                    getActivity().findViewById(R.id.user_profile_stories_empty_text_view).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    protected void setupUserId() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
            setUserId(ParseUser.getCurrentUser().getObjectId());
        } else {
            setUserId(null);
        }
    }

    protected void queryProfile() {

        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.include("avatar");
        userQuery.whereEqualTo("objectId", getUserId());
        userQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        userQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        userQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser != null && parseUser.getObjectId() != null) {
                    queryStories(parseUser);
                } else {
                    Log.i(DailyKind.TAG, "Couldn't find the user profile : " + getUserId());
                }
                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }
            }
        });


    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
