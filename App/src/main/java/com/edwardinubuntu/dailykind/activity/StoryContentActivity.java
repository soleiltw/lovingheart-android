package com.edwardinubuntu.dailykind.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
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

    private ParseObject storyObject;

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

        if (ParseUser.getCurrentUser() != null) {
            findViewById(R.id.story_content_review_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Load user
                    ParseQuery reviewStoryQuery = new ParseQuery("Event");
                    reviewStoryQuery.whereEqualTo("user", ParseUser.getCurrentUser());
                    reviewStoryQuery.whereEqualTo("action", ParseEventTrackingManager.ACTION_REVIEW_STORY);
                    reviewStoryQuery.whereEqualTo("story", storyObject);
                    final ProgressDialog dialog = new ProgressDialog(StoryContentActivity.this);
                    dialog.setMessage(getString(R.string.loading));
                    dialog.show();
                    reviewStoryQuery.getFirstInBackground(new GetCallback() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            dialog.dismiss();
                            openRatingDialog(parseObject);
                        }
                    });


                }
            });
        } else {
            findViewById(R.id.story_content_review_button).setVisibility(View.GONE);
        }

        loadStory();
    }

    private void openRatingDialog(final ParseObject parseObject) {
        int ratingValue = 0;
        if (parseObject!=null && parseObject.has("value")) {
            ratingValue = parseObject.getInt("value");
        }

        final Dialog askRatingsDialog = new Dialog(StoryContentActivity.this);
        askRatingsDialog.setContentView(R.layout.layout_story_ratings);
        askRatingsDialog.setTitle(getString(R.string.story_content_encourage_ratings));
        askRatingsDialog.show();

        final ParseUser parseUser = ParseUser.getCurrentUser();
        TextView userNameTextView = (TextView)askRatingsDialog.findViewById(R.id.user_name_text_view);
        userNameTextView.setText(parseUser.getString("name"));

        ParseObject avatarObject = parseUser.getParseObject("avatar");
        if (avatarObject != null) {
            avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    ImageView avatarImageView = (ImageView)askRatingsDialog.findViewById(R.id.user_avatar_image_view);
                    if (parseObject.getString("imageType").equals("url")) {
                        Picasso.with(getApplicationContext())
                                .load(parseObject.getString("imageUrl"))
                                .transform(new CircleTransform())
                                .into(avatarImageView);
                    }
                }
            });
        }

        final ImageButton rating1Button = (ImageButton)askRatingsDialog.findViewById(R.id.rating_stars_1);
        final ImageButton rating2Button = (ImageButton)askRatingsDialog.findViewById(R.id.rating_stars_2);
        final ImageButton rating3Button = (ImageButton)askRatingsDialog.findViewById(R.id.rating_stars_3);
        final ImageButton rating4Button = (ImageButton)askRatingsDialog.findViewById(R.id.rating_stars_4);
        final ImageButton rating5Button = (ImageButton)askRatingsDialog.findViewById(R.id.rating_stars_5);

        final int[] finalRatingValue = {ratingValue};
        rating1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_0);
                rating3Button.setImageResource(R.drawable.ic_action_star_0);
                rating4Button.setImageResource(R.drawable.ic_action_star_0);
                rating5Button.setImageResource(R.drawable.ic_action_star_0);
                finalRatingValue[0] = 1;
            }
        });

        rating2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_10);
                rating3Button.setImageResource(R.drawable.ic_action_star_0);
                rating4Button.setImageResource(R.drawable.ic_action_star_0);
                rating5Button.setImageResource(R.drawable.ic_action_star_0);
                finalRatingValue[0] = 2;
            }
        });

        rating3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_10);
                rating3Button.setImageResource(R.drawable.ic_action_star_10);
                rating4Button.setImageResource(R.drawable.ic_action_star_0);
                rating5Button.setImageResource(R.drawable.ic_action_star_0);
                finalRatingValue[0] = 3;
            }
        });

        rating4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_10);
                rating3Button.setImageResource(R.drawable.ic_action_star_10);
                rating4Button.setImageResource(R.drawable.ic_action_star_10);
                rating5Button.setImageResource(R.drawable.ic_action_star_0);
                finalRatingValue[0] = 4;
            }
        });

        rating5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_10);
                rating3Button.setImageResource(R.drawable.ic_action_star_10);
                rating4Button.setImageResource(R.drawable.ic_action_star_10);
                rating5Button.setImageResource(R.drawable.ic_action_star_10);
                finalRatingValue[0] = 5;
            }
        });

        switch (ratingValue) {
            case 1:
                rating1Button.performClick();
                break;
            case 2:
                rating2Button.performClick();
                break;
            case 3:
                rating3Button.performClick();
                break;
            case 4:
                rating4Button.performClick();
                break;
            case 5:
                rating5Button.performClick();
                break;
        }

        final EditText commentText = (EditText)askRatingsDialog.findViewById(R.id.story_ratings_comment_edit_text);
        if (parseObject!=null && parseObject.getString("description") != null ){
            commentText.setText(parseObject.getString("description"));
        }

        BootstrapButton submitButton = (BootstrapButton)askRatingsDialog.findViewById(R.id.story_ratings_submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject eventObject;

                if (parseObject != null) {
                    eventObject = parseObject;
                } else {
                    eventObject = new ParseObject("Event");

                    // Maybe user has not login.
                    if (parseUser != null) {
                        eventObject.put("user", parseUser);
                    }


                    eventObject.put("story", storyObject);
                    eventObject.put("action", ParseEventTrackingManager.ACTION_REVIEW_STORY);
                }

                eventObject.put("value", finalRatingValue[0]);

                if (commentText.getText() != null) {
                    eventObject.put("description", commentText.getText().toString());
                }
                eventObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        askRatingsDialog.dismiss();
                        if (e != null) {
                            Log.e(DailyKind.TAG, e.getLocalizedMessage());
                        } else {
                            Log.d(DailyKind.TAG, "Parse event saved. " + ParseEventTrackingManager.ACTION_REVIEW_STORY + " on " + storyObject.getObjectId());
                            loadRatings();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORY_CONTENT_EDIT && resultCode == RESULT_OK) {
            loadStory();
        }
    }

    private void loadRatings() {

        ParseQuery<ParseObject> ratingsQuery = new ParseQuery<ParseObject>("Event");
        ratingsQuery.whereEqualTo("story", storyObject);
        ratingsQuery.whereEqualTo("action", ParseEventTrackingManager.ACTION_REVIEW_STORY);
        ratingsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                findViewById(R.id.story_impact_card_layout).setVisibility(View.VISIBLE);

                if (parseObjects != null && !parseObjects.isEmpty()) {

                    findViewById(R.id.ratings_empty_ask_text_view).setVisibility(View.GONE);
                    findViewById(R.id.ratings_stat_group_layout).setVisibility(View.VISIBLE);

                    int ratingCount = 0;
                    for (ParseObject eachEvent : parseObjects) {
                        ratingCount += eachEvent.getInt("value");
                    }
                    TextView ratingsCountTextView = (TextView)findViewById(R.id.ratings_total_stars_text_view);
                    ratingsCountTextView.setText(String.valueOf(ratingCount));

                    TextView ratingUsersCountTextView = (TextView)findViewById(R.id.ratings_users_numbers_text_view);
                    ratingUsersCountTextView.setText(String.valueOf(parseObjects.size()));
                } else {
                    // Ask to be the first one
                    findViewById(R.id.ratings_empty_ask_text_view).setVisibility(View.VISIBLE);
                    findViewById(R.id.ratings_stat_group_layout).setVisibility(View.GONE);
                }
            }
        });
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

                    storyObject = parseObject;
                    loadRatings();

                    findViewById(R.id.story_content_progress_bar).setVisibility(View.GONE);
                    findViewById(R.id.story_content_review_button).setVisibility(View.VISIBLE);

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
                    if (avatarObject != null) {
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
