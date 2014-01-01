package com.edwardinubuntu.dailykind.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Story;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * Created by edward_chiang on 2014/1/1.
 */
public class StoryContentActivity extends ActionBarActivity {

    private String objectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story_content);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        objectId = getIntent().getStringExtra("objectId");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.include("ideaPointer");
        storyQuery.include("StoryTeller");
        storyQuery.include("graphicPointer");
        storyQuery.whereEqualTo("objectId", this.objectId);
        storyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);
                Story story = parseObjectManager.getStory();

                TextView lastSharedContentTextView = (TextView)findViewById(R.id.me_stories_last_share_content_text_view);
                lastSharedContentTextView.setText(story.getContent());

                TextView lastInspiredTextView = (TextView)findViewById(R.id.me_stories_last_share_inspired_from_text_view);
                lastInspiredTextView.setVisibility(View.GONE);
                if (parseObject.getParseObject("ideaPointer") != null) {
                    story.setIdea(new ParseObjectManager(parseObject.getParseObject("ideaPointer")).getIdea());

                    if (story.getIdea().getName().length() > 0) {

                    lastInspiredTextView.setText(
                            getString(R.string.stories_last_share_inspired_by_text_prefix)+
                                    getString(R.string.space) +
                                    story.getIdea().getName());
                        lastInspiredTextView.setVisibility(View.VISIBLE);
                    }
                }

                TextView lastSharedDateTextView = (TextView)findViewById(R.id.me_stories_last_share_date_Text_view);
                PrettyTime prettyTime = new PrettyTime(new Date());
                lastSharedDateTextView.setText(
                        prettyTime.format(story.getCreatedAt()));

                TextView userNameTextView = (TextView)findViewById(R.id.user_name_text_view);
                if (story.getStoryTeller() != null) {
                    userNameTextView.setText(story.getStoryTeller().getString("name"));
                }
                ParseObject avatarObject = story.getStoryTeller().getParseObject("avatar");
                avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        ImageView avatarImageView = (ImageView)findViewById(R.id.user_avatar_image_view);
                        if (parseObject.getString("imageType").equals("url")) {
                            Picasso.with(getApplicationContext())
                                    .load(parseObject.getString("imageUrl"))
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
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
