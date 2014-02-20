package com.edwardinubuntu.dailykind.fragment;

import com.edwardinubuntu.dailykind.DailyKind;
import com.parse.*;

/**
 * Created by edward_chiang on 2014/2/14.
 */
public abstract class UserProfileFragment extends PlaceholderFragment {

    private String userId;

    public UserProfileFragment() {
        setupUserId();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    protected void setupUserId() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
            setUserId(ParseUser.getCurrentUser().getObjectId());
        } else {
            setUserId(null);
        }
    }

    protected void queryProfile(GetCallback<ParseUser> userGetCallback) {
        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
        userQuery.include("avatar");
        userQuery.whereEqualTo("objectId", getUserId());
        userQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        userQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        userQuery.getFirstInBackground(userGetCallback);
    }

    protected void queryGraphicEarned(ParseUser parseUser, final FindCallback<ParseObject> findCallback) {
        ParseQuery<ParseObject> graphicsEarnedQuery = ParseQuery.getQuery("GraphicsEarned");
        graphicsEarnedQuery.whereEqualTo("userId", parseUser);
        graphicsEarnedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        graphicsEarnedQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    ParseRelation graphicsRelation = parseObject.getRelation("graphicsEarned");
                    ParseQuery<ParseObject> graphicsEarnedQuery = graphicsRelation.getQuery();
                    graphicsEarnedQuery.findInBackground(findCallback);
                }
            }
        });
    }

    protected void queryStories(ParseUser parseUser, FindCallback<ParseObject> findCallback) {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", parseUser);
        storyQuery.orderByDescending("createdAt");
        storyQuery.include("ideaPointer");
        storyQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        storyQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        storyQuery.findInBackground(findCallback);
    }
}
