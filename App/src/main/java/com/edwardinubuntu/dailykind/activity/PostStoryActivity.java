package com.edwardinubuntu.dailykind.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.parse.*;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class PostStoryActivity extends ActionBarActivity {

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(com.edwardinubuntu.dailykind.R.layout.activity_post_story);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getResources().getString(com.edwardinubuntu.dailykind.R.string.story_upload_progress));
        dialog.setIndeterminate(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        findViewById(com.edwardinubuntu.dailykind.R.id.post_story_done_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject parseObject = new ParseObject("Story");

                EditText helperEditText = (EditText)findViewById(com.edwardinubuntu.dailykind.R.id.helper_name_edit_text);
                if (helperEditText.getText() != null) {
                    parseObject.put("HelperName", helperEditText.getText().toString());
                }

                EditText helpedNameEditText = (EditText)findViewById(com.edwardinubuntu.dailykind.R.id.helped_name_edit_text);
                if (helpedNameEditText.getText() != null) {
                    parseObject.put("HelpedName", helpedNameEditText.getText().toString());
                }

                // TODO Check user has login
                parseObject.put("StoryTeller", ParseUser.getCurrentUser());

                EditText contentEditText = (EditText)findViewById(com.edwardinubuntu.dailykind.R.id.content_edit_text);
                parseObject.put("Content", contentEditText.getText().toString());

                dialog.show();
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        finish();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }


}
