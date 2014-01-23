package com.edwardinubuntu.dailykind.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Story;
import com.parse.*;

/**
 * Created by edward_chiang on 2014/1/14.
 */
public class EditStoryActivity extends PostStoryActivity {

    private Story story;

    private ParseObject storyObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        story = (Story)getIntent().getSerializableExtra("storyContent");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(R.id.content_location_area_layout).setVisibility(View.GONE);
        findViewById(R.id.story_image_layout).setVisibility(View.GONE);

        // Add loading view
        findViewById(R.id.loading_progress_layout).setVisibility(View.VISIBLE);
        findViewById(R.id.content_edit_text).setVisibility(View.GONE);

        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("objectId", story.getObjectId());
        storyQuery.include("ideaPointer");
        storyQuery.include("StoryTeller");
        storyQuery.include("graphicPointer");
        storyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                findViewById(R.id.loading_progress_layout).setVisibility(View.GONE);
                findViewById(R.id.content_edit_text).setVisibility(View.VISIBLE);
                if (parseObject != null) {
                    Log.d(DailyKind.TAG, "storyQuery.getFirstInBackground found: " + parseObject.getObjectId());
                    contentEditText.setText(parseObject.getString("Content"));
                    contentEditText.requestFocus();
                }
                if (e!=null) {
                    Log.e(DailyKind.TAG, "storyQuery.getFirstInBackground: " + e.getLocalizedMessage());
                    Toast.makeText(EditStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void postStory() {

        if (contentEditText.getText().length() == 0) {

            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.post_story_content_need_title))
                    .setMessage(getResources().getString(R.string.post_story_content_need_message))
                    .setPositiveButton(getResources().getString(R.string.go), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .show();

            return;
        }

        if (!storyPostingDialog.isShowing()) {
            storyPostingDialog.show();
        }
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("objectId", story.getObjectId());
        storyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e!=null) {
                    Log.e(DailyKind.TAG, "ParseObject.getFirstInBackground: " + e.getLocalizedMessage());
                    Toast.makeText(EditStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (storyPostingDialog.isShowing()) {
                    storyPostingDialog.dismiss();
                }
                // Add what need to change
                parseObject.put("Content", contentEditText.getText().toString());
                submit(parseObject);
            }
        });


    }

    protected void submit(ParseObject parseObject) {

        Log.d(DailyKind.TAG, "parseObject.saveInBackground object id: " + parseObject.getObjectId());

        if (!storyPostingDialog.isShowing()) {
            storyPostingDialog.show();
        }
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(DailyKind.TAG, "ParseObject.saveInBackground: " + e.getLocalizedMessage());
                    Toast.makeText(EditStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
                if (storyPostingDialog.isShowing()) {
                    storyPostingDialog.dismiss();
                }
            }
        });
    }
}
