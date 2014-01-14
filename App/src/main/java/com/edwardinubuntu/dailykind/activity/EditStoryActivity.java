package com.edwardinubuntu.dailykind.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.ParseSettings;
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

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        story = (Story)getIntent().getSerializableExtra("storyContent");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(R.id.content_location_area_layout).setVisibility(View.GONE);

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
                    storyObject = parseObject;
                    contentEditText.setText(parseObject.getString("Content"));

                    if(contentEditText.requestFocus()) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            }
        });
    }

    protected void postStory() {

        if (ParseUser.getCurrentUser() == null) {
            // TODO alert
            return;
        }

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

        // Add what need to change
        storyObject.put("Content", contentEditText.getText().toString());

        submit(storyObject);
    }

    protected void submit(ParseObject parseObject) {
        if (!storyPostingDialog.isShowing()) {
            storyPostingDialog.show();
        }
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e!=null) {
                    if (storyPostingDialog.isShowing()) {
                        storyPostingDialog.dismiss();
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(getApplication(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
