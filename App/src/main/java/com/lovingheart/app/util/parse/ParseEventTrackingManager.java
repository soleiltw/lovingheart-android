package com.lovingheart.app.util.parse;

import android.util.Log;
import com.lovingheart.app.DailyKind;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by edward_chiang on 2014/1/9.
 */
public class ParseEventTrackingManager {

    /**
     * @deprecated
     */
    public static String ACTION_VIEW_STORY = "view_story";


    public static String ACTION_REVIEW_STORY = "review_story";

    public static void event(ParseUser parseUser, final ParseObject storyObject, final String action, final int value) {
        ParseObject eventObject = new ParseObject("Event");

        // Maybe user has not login.
        if (parseUser != null) {
            eventObject.put("user", parseUser);
        }
        eventObject.put("story", storyObject);
        eventObject.put("action", action);
        eventObject.put("value", value);
        eventObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                } else {
                    Log.d(DailyKind.TAG, "Parse event saved. " + action + " on " + storyObject.getObjectId() + " with " + value);
                }
            }
        });
    }
}
