package com.edwardinubuntu.dailykind.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.listener.PressedOnTouchLIstener;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.parse.*;
import com.squareup.picasso.Picasso;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class PostStoryActivity extends ActionBarActivity {

    private ProgressDialog dialog;

    private Idea idea;

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

        idea = (Idea)getIntent().getSerializableExtra("idea");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ParseUser parseUser = ParseUser.getCurrentUser();
        final ImageView storyTellerImageView = (ImageView)findViewById(com.edwardinubuntu.dailykind.R.id.user_avatar_image_view);

        ParseObject avatarObject = parseUser.getParseObject("avatar");
        avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                Picasso.with(getApplicationContext())
                        .load(parseObject.getString("imageUrl"))
                        .placeholder(R.drawable.ic_action_user)
                        .transform(new CircleTransform())
                        .into(storyTellerImageView);
            }
        });

        TextView ideaWasTextView = (TextView)findViewById(R.id.content_idea_number_text_view);
        if (idea!=null) {
            ideaWasTextView.setText(idea.getName());
        }
        ideaWasTextView.setOnTouchListener(new PressedOnTouchLIstener());
        ideaWasTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    private void postStory() {
        final ParseObject parseObject = new ParseObject("Story");

        // TODO Check user has login
        parseObject.put("StoryTeller", ParseUser.getCurrentUser());
        EditText contentEditText = (EditText)findViewById(R.id.content_edit_text);
        parseObject.put("Content", contentEditText.getText().toString());

        if (idea != null) {
            ParseQuery<ParseObject> ideaQuery = new ParseQuery<ParseObject>("Idea");
            ideaQuery.whereEqualTo("objectId", idea.getObjectId());
            dialog.show();
            ideaQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject ideaObjectCallBack, ParseException e) {
                    parseObject.put("ideaPointer", ideaObjectCallBack);

                    ideaObjectCallBack.put("doneCount", ideaObjectCallBack.getInt("doneCount") + 1);
                    ideaObjectCallBack.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {

                        }
                    });

                    submit(parseObject);

                    ParseQuery graphicObjectQuery = new ParseQuery<ParseObject>("GraphicImage");
                    graphicObjectQuery.whereEqualTo("objectId", idea.getGraphic().getObjectId());
                    ParseObject graphicObject = null;
                    try {
                        graphicObject = graphicObjectQuery.getFirst();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    if (graphicObject != null) {
                        // Earn graphic
                        ParseQuery<ParseObject> graphicsEarnedQuery = new ParseQuery<ParseObject>("GraphicsEarned");
                        graphicsEarnedQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
                        final ParseObject finalGraphicObject = graphicObject;
                        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {

                                ParseObject graphicsEarnedObject;
                                if  (parseObject == null) {
                                    graphicsEarnedObject = new ParseObject("GraphicsEarned");
                                    graphicsEarnedObject.put("userId", ParseUser.getCurrentUser());

                                    ParseRelation graphicsRelation = graphicsEarnedObject.getRelation("graphicsEarned");
                                    graphicsRelation.add(finalGraphicObject);

                                } else {
                                    graphicsEarnedObject = parseObject;
                                    ParseRelation graphicsRelation = graphicsEarnedObject.getRelation("graphicsEarned");
                                    graphicsRelation.add(finalGraphicObject);
                                }
                                graphicsEarnedObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e!=null) {
                                            Log.e(DailyKind.TAG, "graphicsEarnedObject.saveInBackground: " + e.getLocalizedMessage());
                                        }
                                    }
                                });
                            }
                        });
                    }


                }
            });
        } else {
            submit(parseObject);
        }
    }

    private void submit(ParseObject parseObject) {
        if (!dialog.isShowing()) {
            dialog.show();
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_post: {
                postStory();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_story, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
