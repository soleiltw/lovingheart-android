package com.edwardinubuntu.dailykind.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.object.Story;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseEventTrackingManager;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2014/1/1.
 */
public class StoryContentActivity extends ActionBarActivity {

    private String objectId;

    private Menu menu;

    private Story story;

    private int STORY_CONTENT_EDIT = 100;

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

        loadStory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORY_CONTENT_EDIT && resultCode == RESULT_OK) {
            loadStory();
        }
    }

    private void loadStory() {
        findViewById(R.id.story_content_progress_bar).setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.include("ideaPointer");
        storyQuery.include("StoryTeller");
        storyQuery.include("graphicPointer");
        storyQuery.whereEqualTo("objectId", this.objectId);
        storyQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        storyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {

                if (parseObject!=null) {

                    findViewById(R.id.story_content_progress_bar).setVisibility(View.GONE);

                    ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);
                    story = parseObjectManager.getStory();

                    TextView lastSharedContentTextView = (TextView)findViewById(R.id.me_stories_last_share_content_text_view);
                    lastSharedContentTextView.setText(story.getContent());

                    TextView lastInspiredTextView = (TextView)findViewById(R.id.me_stories_last_share_inspired_from_text_view);
                    lastInspiredTextView.setVisibility(View.GONE);
                    if (parseObject.getParseObject("ideaPointer") != null) {
                        story.setIdea(new ParseObjectManager(parseObject.getParseObject("ideaPointer")).getIdea());

                        if (parseObject.getParseObject("ideaPointer").getParseObject("categoryPointer") != null) {
                            ParseObject categoryObject = parseObject.getParseObject("ideaPointer").getParseObject("categoryPointer");

                            ParseQuery<ParseObject> categoryQuery = new ParseQuery<ParseObject>("Category");
                            categoryQuery.whereEqualTo("objectId", categoryObject.getObjectId());
                            categoryQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                            categoryQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (parseObject !=null) {
                                        Category category = new ParseObjectManager(parseObject).getCategory();

                                        TextView categoryTextView = (TextView)findViewById(R.id.story_content_category_text_view);
                                        if (category.getName() != null) {
                                            categoryTextView.setVisibility(View.VISIBLE);
                                            categoryTextView.setText(category.getName());
                                        }
                                    }
                                }
                            });
                        }

                        if (story.getIdea().getName().length() > 0) {
                            lastInspiredTextView.setText( story.getIdea().getName());
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

                        // Display edit button
                        if (ParseUser.getCurrentUser() != null &&
                                story.getStoryTeller().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {

                            if (menu != null) {
                                MenuItem editStoryItem = menu.findItem(R.id.action_edit_story);
                                editStoryItem.setVisible(true);
                            }
                        }
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

                    ImageView storyContentImageView = (ImageView)findViewById(R.id.me_stories_image_view);
                    // Check if have graphic
                    if (parseObject.getParseObject("graphicPointer") != null) {
                        Graphic graphic = new ParseObjectManager(parseObject.getParseObject("graphicPointer")).getGraphic();
                        story.setGraphic(graphic);

                        if (story.getGraphic() !=null && story.getGraphic().getParseFileUrl() != null) {

                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            LinearLayout.LayoutParams storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)storyContentImageView.getLayoutParams();
                            storyContentImageViewLayoutParams.width = displayMetrics.widthPixels;
                            storyContentImageViewLayoutParams.height = displayMetrics.widthPixels;
                            storyContentImageView.requestLayout();
                            storyContentImageView.setVisibility(View.VISIBLE);

                            Picasso.with(getApplicationContext())
                                    .load(story.getGraphic().getParseFileUrl())
                                    .placeholder(R.drawable.card_default)
                                    .resize(storyContentImageViewLayoutParams.width, storyContentImageViewLayoutParams.height)
                                    .into(storyContentImageView);
                        }
                    } else {
                        storyContentImageView.setVisibility(View.GONE);
                    }

                    TextView locationAreaNameTextView = (TextView)findViewById(R.id.user_activity_location_area_name_text_view);
                    locationAreaNameTextView.setText(story.getLocationAreaName());

                    ParseEventTrackingManager.event(
                            ParseUser.getCurrentUser(),
                            parseObject,
                            ParseEventTrackingManager.ACTION_VIEW_STORY,
                            1
                    );
                    updateStoryViewCount(parseObject);
                }

            }
        });
    }

    private void updateStoryViewCount(final ParseObject parseObject) {
        // Update View Count
        ParseQuery<ParseObject> viewEventQuery = new ParseQuery<ParseObject>("Event");
        viewEventQuery.whereEqualTo("story", parseObject);
        viewEventQuery.whereEqualTo("action", ParseEventTrackingManager.ACTION_VIEW_STORY);

        viewEventQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                int viewCount = 0;

                for (ParseObject eventObject : parseObjects) {
                    viewCount += eventObject.getInt("value");
                }

                parseObject.put("viewCount", viewCount);

                final int finalViewCount = viewCount;
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e==null) {
                            Log.d(DailyKind.TAG, "Story view count update: " + finalViewCount);
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_content, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_edit_story: {
                if ( story!=null ) {
                    Intent editStoryIntent = new Intent(this, EditStoryActivity.class);

                    Story editStoryObject = new Story();
                    editStoryObject.setGraphic(this.story.getGraphic());
                    editStoryObject.setIdea(this.story.getIdea());
                    editStoryObject.setContent(this.story.getContent());
                    editStoryObject.setObjectId(this.story.getObjectId());

                    editStoryIntent.putExtra("storyContent", editStoryObject);

                    startActivityForResult(editStoryIntent, STORY_CONTENT_EDIT);
                }
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
