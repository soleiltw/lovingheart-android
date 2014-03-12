package com.lovingheart.app.util.parse;

import android.util.Log;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.object.Category;
import com.lovingheart.app.object.Graphic;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.object.Story;
import com.parse.*;

/**
 * Created by edward_chiang on 2013/12/29.
 */
public class ParseObjectManager {

    private ParseObject parseObject;

    public static final String USER_NAME = "name";

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
}
