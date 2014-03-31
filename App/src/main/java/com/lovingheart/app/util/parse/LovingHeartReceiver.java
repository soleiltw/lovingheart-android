package com.lovingheart.app.util.parse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.activity.DeedContentActivity;
import com.lovingheart.app.activity.StoryContentActivity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by edward_chiang on 2014/3/31.
 */
public class LovingHeartReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null && intent.getExtras().getString("com.parse.Data") != null) {
            try {
                JSONObject jsonObject = new JSONObject(intent.getExtras().getString("com.parse.Data"));
                if (jsonObject.has("intent") && jsonObject.getString("intent").equals("StoryContentActivity")) {
                    Intent openStoryContent = new Intent(context.getApplicationContext(), StoryContentActivity.class);
                    openStoryContent.putExtra("objectId", jsonObject.getString("objectId"));
                    openStoryContent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(openStoryContent);
                } else if (jsonObject.has("intent") && jsonObject.getString("intent").equals("DeedContentActivity")) {
                    Intent openIdeaContent = new Intent(context.getApplicationContext(), DeedContentActivity.class);
                    openIdeaContent.putExtra("ideaObjectId", jsonObject.getString("objectId"));
                    openIdeaContent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(openIdeaContent);
                }
            } catch (JSONException jsonException) {
                Log.e(DailyKind.TAG, "JSONException: " + jsonException.getLocalizedMessage());
            }
        }
    }
}
