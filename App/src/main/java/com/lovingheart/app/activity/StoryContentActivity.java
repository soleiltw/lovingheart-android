package com.lovingheart.app.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.ReviewArrayAdapter;
import com.lovingheart.app.object.*;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.lovingheart.app.util.CircleTransform;
import com.lovingheart.app.util.parse.ParseEventTrackingManager;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.*;

/**
 * Created by edward_chiang on 2014/1/1.
 */
public class StoryContentActivity extends ActionBarActivity {

    private String objectId;

    private Menu menu;

    private Story story;

    private ParseObject storyObject;

    private LinearLayout.LayoutParams storyContentImageViewLayoutParams;

    private ImageView storyContentImageView;

    private int STORY_CONTENT_EDIT = 100;
    private int ASK_USER_LOGIN = 110;

    private int finalRatingValue;

    private boolean hasBeenUpdateReviews;

    private ReviewArrayAdapter reviewArrayAdapter;

    private List<Review> reviewList;

    private View ideaViewGroup;

    private String openedFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_story_content);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        objectId = getIntent().getStringExtra("objectId");

        hasBeenUpdateReviews = false;

        reviewList = new ArrayList<Review>();
        reviewArrayAdapter = new ReviewArrayAdapter(this, android.R.layout.simple_list_item_1, reviewList);

        openedFrom = getIntent().getStringExtra("OpenedFrom");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        storyContentImageView = (ImageView)findViewById(R.id.me_stories_image_view);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)storyContentImageView.getLayoutParams();
        storyContentImageViewLayoutParams.width = displayMetrics.widthPixels;
        storyContentImageViewLayoutParams.height = displayMetrics.widthPixels;
        storyContentImageView.requestLayout();

        ExpandableListView reviewsListView = (ExpandableListView)findViewById(R.id.story_content_review_list_view);
        reviewsListView.setExpand(true);
        reviewsListView.setAdapter(reviewArrayAdapter);
        reviewsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < reviewList.size()) {
                    Review review = reviewList.get(position);
                    Intent userProfileIntent = new Intent(StoryContentActivity.this, UserProfileActivity.class);
                    userProfileIntent.putExtra("userId", review.getUser().getUserId());
                    startActivity(userProfileIntent);
                }
            }
        });

        ideaViewGroup = findViewById(R.id.story_idea_group_layout);

        loadStory();
    }


    private View.OnClickListener loginUserGiveReviewListener = new View.OnClickListener() {
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
                    if (e == null && parseObject!= null) {
                        openRatingDialog(parseObject);
                    } else {
                        openRatingDialog();
                    }

                }
            });


        }
    };

    private View.OnClickListener askUserLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            CheckUserLoginUtil.askLoginDialog(StoryContentActivity.this, StoryContentActivity.this);

        }
    };

    private void storyReviewSetup() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getString("name") != null) {
            findViewById(R.id.story_content_review_button).setOnClickListener(loginUserGiveReviewListener);
        } else {
            findViewById(R.id.story_content_review_button).setOnClickListener(askUserLoginListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == STORY_CONTENT_EDIT && resultCode == RESULT_OK) {
            loadStory();
        }
        else if (requestCode == ASK_USER_LOGIN && resultCode == RESULT_OK) {
            storyReviewSetup();
        }
        if (this != null && data != null) {
            Session.getActiveSession()
                    .onActivityResult(this, requestCode, resultCode, data);
        }
    }

    private void loadRatings() {

        ParseQuery<ParseObject> ratingsQuery = new ParseQuery<ParseObject>("Event");
        ratingsQuery.whereEqualTo("story", storyObject);
        ratingsQuery.whereEqualTo("action", ParseEventTrackingManager.ACTION_REVIEW_STORY);
        ratingsQuery.include("user");
        ratingsQuery.addDescendingOrder("createdAt");
        ratingsQuery.clearCachedResult();
        ratingsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                findViewById(R.id.story_impact_card_layout).setVisibility(View.VISIBLE);

                if (parseObjects != null && !parseObjects.isEmpty()) {

                    findViewById(R.id.ratings_empty_ask_text_view).setVisibility(View.GONE);
                    findViewById(R.id.ratings_stat_group_layout).setVisibility(View.VISIBLE);

                    int ratingCount = 0;
                    reviewList.clear();
                    for (ParseObject eachEvent : parseObjects) {
                        Review review = new Review();

                        Log.d(DailyKind.TAG, "EachEvent: " + eachEvent.toString());

                        review.setUserObject(eachEvent.getParseUser("user"));
                        review.setValue(eachEvent.getInt("value"));
                        review.setReviewDescription(eachEvent.getString("description"));
                        review.setCreatedAt(eachEvent.getCreatedAt());

                        User user = new User();
                        if (eachEvent.getParseUser("user")!=null) {
                            user.setName(eachEvent.getParseUser("user").getString("name"));
                            user.setUserId(eachEvent.getParseUser("user").getObjectId());
                            if (eachEvent.getParseUser("user") != null
                                    && eachEvent.getParseUser("user").has("avatar")
                                    && eachEvent.getParseUser("user").getParseObject("avatar")!=null) {
                                user.setAvatar(eachEvent.getParseUser("user").getParseObject("avatar"));
                            }
                            review.setUser(user);
                        }
                        reviewList.add(review);

                        ratingCount += eachEvent.getInt("value");
                    }
                    reviewArrayAdapter.notifyDataSetChanged();
                    storyObject.put("reviewImpact", ratingCount);
                    if (storyObject.getParseUser("StoryTeller").getObjectId().
                            equals(ParseUser.getCurrentUser().getObjectId())) {
                        if (!hasBeenUpdateReviews) {
                            hasBeenUpdateReviews = true;
                            // Avoid to call several time
                            storyObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    Log.d(DailyKind.TAG, "Story object save review impact");
                                    if (e != null) {
                                        hasBeenUpdateReviews = false;
                                        Log.e(DailyKind.TAG, e.getLocalizedMessage());
                                    }
                                }
                            });
                        } else {
                            Log.d(DailyKind.TAG, "Skip update reviews.");
                        }
                    } else {
                        Log.d(DailyKind.TAG, "It's not yours, can't update the reviews count.");
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
        storyQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        storyQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {

                if (parseObject!=null) {

                    storyObject = parseObject;
                    loadRatings();

                    storyReviewSetup();

                    findViewById(R.id.story_content_progress_bar).setVisibility(View.GONE);
                    findViewById(R.id.story_content_review_button).setVisibility(View.VISIBLE);

                    ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);
                    story = parseObjectManager.getStory();

                    TextView lastSharedContentTextView = (TextView)findViewById(R.id.me_stories_last_share_content_text_view);

                    lastSharedContentTextView.setText(story.getContent());

                    TextView lastInspiredTextView = (TextView)findViewById(R.id.me_stories_last_share_inspired_from_text_view);
                    lastInspiredTextView.setVisibility(View.GONE);
                    if (parseObject.getParseObject("ideaPointer") != null) {
                        final ParseObject ideaObject = parseObject.getParseObject("ideaPointer");
                        story.setIdea(new ParseObjectManager(ideaObject).getIdea());

                        if (ideaObject.getParseObject("categoryPointer") != null) {
                            ParseObject categoryObject = ideaObject.getParseObject("categoryPointer");

                            ParseQuery<ParseObject> categoryQuery = new ParseQuery<ParseObject>("Category");
                            categoryQuery.whereEqualTo("objectId", categoryObject.getObjectId());
                            categoryQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                            categoryQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
                            categoryQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (parseObject !=null) {
                                        Category category = new ParseObjectManager(parseObject).getCategory();

                                        TextView categoryTextView = (TextView)findViewById(R.id.story_content_category_text_view);
                                        if (category.getName() != null) {
                                            categoryTextView.setVisibility(View.VISIBLE);
                                            categoryTextView.setText(category.getName());

                                            categoryTextView.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Intent categoryIdeaIntent = new Intent(StoryContentActivity.this, DeedContentActivity.class);
                                                    categoryIdeaIntent.putExtra("ideaObjectId", ideaObject.getObjectId());
                                                    startActivity(categoryIdeaIntent);
                                                }
                                            });
                                        }
                                    }
                                }
                            });
                        }

                        if (story.getIdea().getName().length() > 0) {
                            lastInspiredTextView.setText( story.getIdea().getName());
                            lastInspiredTextView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        ideaViewGroup.setVisibility(View.GONE);
                    }

                    TextView lastSharedDateTextView = (TextView)findViewById(R.id.created_at_text_view);
                    PrettyTime prettyTime = new PrettyTime(new Date());
                    lastSharedDateTextView.setText(
                            prettyTime.format(story.getCreatedAt()));

                    TextView userNameTextView = (TextView)findViewById(R.id.user_name_text_view);
                    if (story.getStoryTeller() != null) {
                        if (!story.isAnonymous()) {
                            userNameTextView.setText(story.getStoryTeller().getString("name"));
                        } else {
                            userNameTextView.setText(getString(R.string.story_teller_anonymous));
                        }

                        // Display edit button
                        if (ParseUser.getCurrentUser() != null &&
                                story.getStoryTeller().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {

                            if (menu != null) {
                                MenuItem editStoryItem = menu.findItem(R.id.action_edit_story);
                                editStoryItem.setVisible(true);

                                MenuItem deleteItem = menu.findItem(R.id.action_delete);
                                deleteItem.setVisible(true);
                            }
                        }
                    }

                    final ImageView avatarImageView = (ImageView) findViewById(R.id.user_avatar_image_view);

                    if (!story.isAnonymous()) {
                        avatarImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(getApplicationContext(), UserProfileActivity.class);
                                userIntent.putExtra("userId", story.getStoryTeller().getObjectId());
                                startActivity(userIntent);
                            }
                        });

                        ParseObject avatarObject = story.getStoryTeller().getParseObject("avatar");
                        if (avatarObject != null) {
                            avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (parseObject.getString("imageType").equals("url") && !story.isAnonymous()) {
                                        Picasso.with(getApplicationContext())
                                                .load(parseObject.getString("imageUrl"))
                                                .transform(new CircleTransform())
                                                .into(avatarImageView);
                                    }

                                }
                            });
                        }
                    } else {
                        avatarImageView.setImageResource(R.drawable.ic_action_emo_cool);
                    }

                    // Check if have graphic
                    if (parseObject.getParseObject("graphicPointer") != null) {
                        Graphic graphic = new ParseObjectManager(parseObject.getParseObject("graphicPointer")).getGraphic();
                        story.setGraphic(graphic);

                        if (story.getGraphic() !=null && story.getGraphic().getParseFileUrl() != null) {

                            storyContentImageView.setVisibility(View.VISIBLE);


                            Log.d(DailyKind.TAG, "Story.getGraphic().getParseFileUrl(): " + story.getGraphic().getParseFileUrl());

                            Picasso.with(getApplicationContext())
                                    .load(story.getGraphic().getParseFileUrl())
                                    .placeholder(R.drawable.card_default)
                                    .resize(storyContentImageViewLayoutParams.width, storyContentImageViewLayoutParams.height)
                                    .centerCrop()
                                    .into(storyContentImageView);

                            storyContentImageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent imageIntent = new Intent(StoryContentActivity.this, ImageViewActivity.class);
                                    imageIntent.putExtra("imageUrl", story.getGraphic().getParseFileUrl());
                                    startActivity(imageIntent);
                                }
                            });
                        }
                    }

                    TextView locationAreaNameTextView = (TextView)findViewById(R.id.user_activity_location_area_name_text_view);
                    if (story.getLocationAreaName() != null) {
                        locationAreaNameTextView.setText(
                                getString(R.string.location_area_name_from) + getString(R.string.space) +
                                        story.getLocationAreaName());
                    }
                }

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
            case R.id.action_share: {

                shareStory();

                break;
            }
            case R.id.action_delete: {
                AlertDialog alertDialog;

                AlertDialog.Builder askDeleteDialogBuilder = new AlertDialog.Builder(this);
                askDeleteDialogBuilder.setTitle(getString(R.string.story_content_delete));
                askDeleteDialogBuilder.setMessage(getString(R.string.story_content_delete_this_story));

                askDeleteDialogBuilder.setPositiveButton(getString(R.string.story_content_delete), new DialogInterface.OnClickListener() {
                    /**
                     * This method will be invoked when a button in the dialog is clicked.
                     *
                     * @param dialog The dialog that received the click.
                     * @param which  The button that was clicked (e.g.
                     *               {@link android.content.DialogInterface#BUTTON1}) or the position
                     */
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        storyObject.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    dialog.dismiss();
                                    finish();
                                } else {
                                    Toast.makeText(StoryContentActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });

                askDeleteDialogBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                alertDialog = askDeleteDialogBuilder.create();
                alertDialog.show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareStory() {

        ParseEventTrackingManager.event(ParseUser.getCurrentUser(),
                this.storyObject,
                ParseEventTrackingManager.ACTION_SHARE_STORY_TO_FACEBOOK, 1);

        String imageUrl = new String();
        if (story.getGraphic() != null) {

            if ("url".equalsIgnoreCase(story.getGraphic().getFileType())) {
                imageUrl = story.getGraphic().getImageUrl();
            } else if ("file".equalsIgnoreCase(story.getGraphic().getFileType())) {
                imageUrl = story.getGraphic().getParseFileUrl();
            }
        }

        final Bundle facebookShareParams = new Bundle();
        if (!story.isAnonymous()) {
            facebookShareParams.putString("name", story.getStoryTeller().getString("name"));
        } else {
            facebookShareParams.putString("name", getString(R.string.story_teller_anonymous));
        }
        facebookShareParams.putString("caption", storyObject.getString("Content"));
        if (story.getIdea() != null) {
            facebookShareParams.putString("description", story.getIdea().getName());
        } else {
            facebookShareParams.putString("description", "From LovingHeart for Android.");
        }

        facebookShareParams.putString("link", "http://tw.lovingheartapp.com/story/"+story.getObjectId());
        facebookShareParams.putString("picture", imageUrl);

        Session session = Session.openActiveSession(StoryContentActivity.this, true, new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {

                if (session.isOpened()) {

                    // Invoke the dialog
                    WebDialog feedDialog = (
                            new WebDialog.FeedDialogBuilder(StoryContentActivity.this,
                                    Session.getActiveSession(),
                                    facebookShareParams))
                            .setOnCompleteListener(new WebDialog.OnCompleteListener() {
                                @Override
                                public void onComplete(Bundle values,
                                                       FacebookException error) {
                                    if (error == null) {
                                        // When the story is posted, echo the success
                                        // and the post Id.
                                        final String postId = values.getString("post_id");
                                        if (postId != null) {
                                            Toast.makeText(StoryContentActivity.this,
                                                    getString(R.string.story_facebook_shared),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (error.getLocalizedMessage() != null && error.getLocalizedMessage().length() > 0) {
                                            Toast.makeText(getBaseContext(),
                                                    error.getLocalizedMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                            })
                            .build();
                    if (!(StoryContentActivity.this).isFinishing()) {
                        feedDialog.show();
                    }
                }
            }
        });
        session.addCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                if (state == SessionState.OPENING) {
                    LoginButton loginButton = new LoginButton(StoryContentActivity.this);
                    loginButton.performClick();
                }
            }
        });
    }

    private void openRatingDialog() {
        ParseObject event = new ParseObject("Event");
        event.put("user", ParseUser.getCurrentUser());

        openRatingDialog(event);
    }

    private void openRatingDialog(final ParseObject parseReviewObject) {
        finalRatingValue = 0;
        if (parseReviewObject!=null && parseReviewObject.has("value")) {
            finalRatingValue = parseReviewObject.getInt("value");
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

        rating1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating1Button.setImageResource(R.drawable.ic_action_star_10);
                rating2Button.setImageResource(R.drawable.ic_action_star_0);
                rating3Button.setImageResource(R.drawable.ic_action_star_0);
                rating4Button.setImageResource(R.drawable.ic_action_star_0);
                rating5Button.setImageResource(R.drawable.ic_action_star_0);
                finalRatingValue = 1;
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
                finalRatingValue = 2;
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
                finalRatingValue = 3;
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
                finalRatingValue = 4;
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
                finalRatingValue = 5;
            }
        });

        switch (finalRatingValue) {
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
        if (parseReviewObject!=null && parseReviewObject.getString("description") != null ){
            commentText.setText(parseReviewObject.getString("description"));
        }

        final BootstrapButton submitButton = (BootstrapButton)askRatingsDialog.findViewById(R.id.story_ratings_submit_button);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Maybe user has not login.
                if (parseUser != null) {
                    parseReviewObject.put("user", parseUser);
                }

                parseReviewObject.put("story", storyObject);
                parseReviewObject.put("action", ParseEventTrackingManager.ACTION_REVIEW_STORY);

                ParseACL parseACL = new ParseACL();
                parseACL.setPublicWriteAccess(true);
                parseACL.setPublicReadAccess(true);
                parseReviewObject.setACL(parseACL);
                parseReviewObject.put("value", finalRatingValue);

                if (commentText.getText() != null) {
                    parseReviewObject.put("description", commentText.getText().toString());
                }
                final ProgressDialog dialog = new ProgressDialog(StoryContentActivity.this);
                dialog.setMessage(getString(R.string.loading));
                dialog.show();
                parseReviewObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {

                        dialog.dismiss();

                        if (e != null) {
                            Log.e(DailyKind.TAG, e.getLocalizedMessage());
//                            Toast.makeText(StoryContentActivity.this, getString(R.string.toast_error_message_try_again), Toast.LENGTH_SHORT).show();
                            submitButton.performClick();
                        } else {
                            Log.d(DailyKind.TAG, "Parse event saved. " + ParseEventTrackingManager.ACTION_REVIEW_STORY + " on " + storyObject.getObjectId());

                            ParseObjectManager.userLogDone("bmqzUROe44");
                            ParseObjectManager.userLogDone("ftS0XExWCq");

                            ParseQuery pushQuery = ParseInstallation.getQuery();

                            ArrayList<ParseUser> pushToUsers = new ArrayList<ParseUser>();

                            // Add story teller
                            if (!storyObject.getParseUser("StoryTeller").getObjectId().equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())) {
                                pushToUsers.add(storyObject.getParseUser("StoryTeller"));
                            }

                            // Add comment
                            for (Review eachReview : reviewList) {
                                if (!pushToUsers.contains(eachReview.getUser()) &&
                                        !eachReview.getUserObject().getObjectId().equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())) {
                                    pushToUsers.add(eachReview.getUserObject());
                                }
                            }
//                            pushQuery.whereEqualTo("user", storyObject.getParseUser("StoryTeller"));

                            pushQuery.whereContainedIn("user", pushToUsers);
                            pushQuery.whereEqualTo("user", storyObject.getParseUser("StoryTeller"));

                            ParsePush push = new ParsePush();
                            push.setQuery(pushQuery);

                            StringBuffer message = new StringBuffer();
                            message.append(ParseUser.getCurrentUser().getString("name")
                                    + getString(R.string.space)
                                    + getString(R.string.story_content_push_give_prefix)
                                    + getString(R.string.space)
                                    + finalRatingValue
                                    + getString(R.string.space)
                                    + getString(R.string.story_content_push_give_post));
                            if (commentText.getText() != null && commentText.getText().toString().length() > 0) {

                                message.append(
                                        getString(R.string.space)
                                                + getString(R.string.story_content_push_msg_prefix)
                                                + getString(R.string.space)
                                                + commentText.getText().toString()
                                                + getString(R.string.story_content_push_msg_post)
                                );
                            }
                            push.setMessage(message.toString());
                            Map<String, String> pushMap = new HashMap<String, String>();
                            pushMap.put("action", "com.lovingheart.app.PUSH_STORY");
                            pushMap.put("intent", "StoryContentActivity");
                            pushMap.put("alert", message.toString());
                            pushMap.put("objectId", objectId);
                            JSONObject pushData = new JSONObject(pushMap);
                            push.setData(pushData);
                            push.sendInBackground(new SendCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.e(DailyKind.TAG, "Send push error: " + e.getLocalizedMessage());
                                    }
                                }
                            });


                            askRatingsDialog.dismiss();

                            askToShare();

                            loadRatings();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);

        if (openedFrom != null && openedFrom.equalsIgnoreCase("PostStoryActivity")) {
            askToShare();
        }
    }

    private void askToShare() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dontAskToShare = sharedPreferences.getBoolean(DailyKind.PREFERENCE_DONT_ASK_TO_SHARE, false);
        if (dontAskToShare) return;

        // Ask to share
        AlertDialog alertDialog;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StoryContentActivity.this);
        alertDialogBuilder.setMessage(getString(R.string.ask_share_story_dialog_message));
        alertDialogBuilder.setPositiveButton(getString(R.string.go), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                shareStory();
            }
        })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
        ;
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
