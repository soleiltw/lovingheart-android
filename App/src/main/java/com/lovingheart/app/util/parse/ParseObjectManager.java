package com.lovingheart.app.util.parse;

import android.util.Log;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.object.*;
import com.parse.*;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class ParseObjectManager {

    private ParseObject parseObject;

    public static final String USER_NAME = "name";

    public ParseObjectManager() {
    }

    public ParseObjectManager(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public static void userLogDone(final String targetObjectId) {
        if (ParseUser.getCurrentUser() == null) return;
        // Update user log
        ParseQuery<ParseObject> userLogQuery = new ParseQuery<ParseObject>("UserLog");
        userLogQuery.whereEqualTo("targetObjectClass", "GettingStarted");
        userLogQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
        userLogQuery.whereEqualTo("targetObjectId", targetObjectId);
//        userLogQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
//        userLogQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        userLogQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject == null) {
                    ParseObject userLogObject = new ParseObject("UserLog");
                    userLogObject.put("userId", ParseUser.getCurrentUser());
                    userLogObject.put("targetObjectClass", "GettingStarted");
                    userLogObject.put("targetObjectId", targetObjectId);
                    userLogObject.put("action", "done");
                    userLogObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e==null) {
                                Log.d(DailyKind.TAG, "User Log saved. " + targetObjectId);
                            }
                        }
                    });
                }
            }
        });
    }

    public Story getStory() {
        Story story = new Story();
        story.setObjectId(parseObject.getObjectId());
        story.setStoryTeller(parseObject.getParseUser("StoryTeller"));
        story.setContent(parseObject.getString("Content"));
        story.setCreatedAt(parseObject.getCreatedAt());
        story.setUpdatedAt(parseObject.getUpdatedAt());
        story.setLocationAreaName(parseObject.getString("areaName"));
        story.setViewCount(parseObject.getInt("viewCount"));
        story.setStatus(parseObject.getString("status"));
        return story;
    }

    public Idea getIdea() {
        Idea idea = new Idea();
        idea.setObjectId(parseObject.getObjectId());
        idea.setName(parseObject.getString("Name"));
        idea.setIdeaDescription(parseObject.getString("Description"));
        idea.setDoneCount(parseObject.getInt("doneCount"));

        return idea;
    }

    public Graphic getGraphic() {
        if (parseObject != null) {
            try {
                parseObject.fetchIfNeeded();
            } catch (ParseException e) {
                Log.e(DailyKind.TAG, "Fetch if needed" + e.getLocalizedMessage());
            }
            Graphic graphic = new Graphic();
            if (parseObject.has("imageFile") && parseObject.getParseFile("imageFile") != null) {
                graphic.setParseFileUrl(parseObject.getParseFile("imageFile").getUrl());
            }
            graphic.setFileType(parseObject.getString("imageType"));
            graphic.setObjectId(parseObject.getObjectId());
            graphic.setImageUrl(parseObject.getString("imageUrl"));
            return graphic;
        }
        return null;
    }

    public User getUser() {
        if (parseObject != null) {
            try {
                parseObject.fetchIfNeeded();
            } catch (ParseException e) {
                Log.e(DailyKind.TAG, "Fetch if needed" + e.getLocalizedMessage());
            }
            User user = new User();
            user.setUserId(parseObject.getObjectId());
            user.setName(parseObject.getString("name"));
            user.setAvatar(parseObject.getParseObject("avatar"));

            return user;
        }
        return null;
    }

    public Category getCategory() {
        if (parseObject != null) {
            try {
                parseObject.fetchIfNeeded();
            } catch (ParseException e) {
                Log.e(DailyKind.TAG, "Fetch if needed" + e.getLocalizedMessage());
            }
            Category category = new Category();
            category.setObjectId(parseObject.getObjectId());
            category.setName(parseObject.getString("Name"));

            return category;
        }
        return null;
    }

    public boolean checkPremium(ParseUser parseUser) {
        boolean isPremium = false;
        if (parseUser.has("premium")) {
            String noCheck = parseUser.getString("premium");
            if (noCheck !=null && DailyKind.PARSE_PREMIUM_NOCHECK.equalsIgnoreCase(noCheck)) {
                isPremium = true;
                Log.d(DailyKind.TAG, "Is Premium Feature.");
            }
        }
        return isPremium;
    }
}
