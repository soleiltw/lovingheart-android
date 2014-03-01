package com.lovingheart.app.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.StoryArrayAdapter;
import com.lovingheart.app.listener.LoadMoreListener;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedContentActivity extends ActionBarActivity {

    private String ideaObjectId;

    private ImageView contentImageView;

    private LinearLayout.LayoutParams contentImageViewLayoutParams;

    private TextView numberOfPeopleTextView;

    private TextView earnDescribeTextView;

    private Idea idea;

    private ParseObject ideaObject;

    private View progressBarView;

    private StoryArrayAdapter storyArrayAdapter;

    private List<ParseObject> userActivities;

    private BootstrapButton storiesButton;

    private View dialogProgressBarView;

    private View.OnClickListener askUserLoginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CheckUserLoginUtil.askLoginDialog(DeedContentActivity.this, DeedContentActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        setContentView(R.layout.activity_good_deed_content);

        ideaObjectId = getIntent().getStringExtra("ideaObjectId");

        userActivities = new ArrayList<ParseObject>();

        storyArrayAdapter = new StoryArrayAdapter(DeedContentActivity.this, android.R.layout.simple_list_item_1, userActivities);
        storyArrayAdapter.setLoadMoreListener(new LoadMoreListener() {
            @Override
            public void notifyLoadMore() {
                loadStories(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CheckUserLoginUtil.ASK_USER_LOGIN && resultCode == RESULT_OK) {
            actionButtonSetup();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        numberOfPeopleTextView = (TextView)findViewById(R.id.number_of_people_involved_text_view);

        actionButtonSetup();

        storiesButton = (BootstrapButton)findViewById(R.id.good_deed_content_stories_button);
        storiesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadStories(false);

                Dialog storyDialog = new Dialog(DeedContentActivity.this);

                storyDialog.setContentView(R.layout.fragment_stories);
                storyDialog.setTitle(idea.getName());
                storyDialog.setCanceledOnTouchOutside(true);

                dialogProgressBarView = storyDialog.findViewById(R.id.loading_progress_bar);

                ListView storyListView = (ListView) storyDialog.findViewById(R.id.user_activities_list_view);
                storyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent storyContentIntent = new Intent(DeedContentActivity.this, StoryContentActivity.class);
                        ParseObject activity = userActivities.get(position);
                        storyContentIntent.putExtra("objectId", activity.getObjectId());
                        startActivity(storyContentIntent);
                    }
                });
                storyListView.setAdapter(storyArrayAdapter);

                storyDialog.show();
            }
        });

        contentImageView = (ImageView)findViewById(R.id.story_content_image_view);

        earnDescribeTextView = (TextView)findViewById(R.id.deed_content_earn_description_text_view);

        progressBarView = findViewById(R.id.good_content_progress_bar);

        loadIdea();
    }

    protected void loadStories(final boolean more) {
        final ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Story");
        parseQuery.include("StoryTeller");
        parseQuery.orderByDescending("createdAt");
        parseQuery.include("ideaPointer");
        parseQuery.include("graphicPointer");
        parseQuery.setLimit(DailyKind.PARSE_QUERY_LIMIT);
        parseQuery.whereContainedIn("language", DailyKind.getLanguageCollection(DeedContentActivity.this));
        parseQuery.whereEqualTo("ideaPointer", ideaObject);
        parseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);

        ParseQuery storyCountQuery = ParseQuery.getQuery("Story");
        storyCountQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        storyCountQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        storyCountQuery.whereContainedIn("language", DailyKind.getLanguageCollection(DeedContentActivity.this));
        storyCountQuery.whereEqualTo("ideaPointer", ideaObject);
        storyCountQuery.countInBackground(new CountCallback() {
            @Override
            public void done(final int totalCount, ParseException e) {

                ideaObject.put("doneCount", totalCount);
                ideaObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Log.d(DailyKind.TAG, "Idea total Count update: " + totalCount);
                    }
                });

                if (more) {
                    if (totalCount > userActivities.size()) {
                        parseQuery.setSkip(userActivities.size());
                        if (dialogProgressBarView != null) {
                            dialogProgressBarView.setVisibility(View.VISIBLE);
                        }
                        queryToCallBack(parseQuery);
                    } else {
                        Log.d(DailyKind.TAG, "End of query.");
                        storyArrayAdapter.setLoadMoreEnd(true);
                    }
                }

            }
        });

        if (!more) {
            userActivities.clear();
            if (dialogProgressBarView != null) {
                dialogProgressBarView.setVisibility(View.VISIBLE);
            }
            queryToCallBack(parseQuery);
        }
    }

    protected void queryToCallBack(ParseQuery<ParseObject> parseQuery) {

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {

                if (parseObjects!=null) {

                    boolean dataHasChange = false;
                    for (ParseObject eachParseObject : parseObjects) {
                        boolean hasAdd = false;
                        // If use cache then network, then done will be call 2 times.
                        for (ParseObject addedParseObject : userActivities) {
                            if (eachParseObject.getObjectId().equals(addedParseObject.getObjectId())) {
                                hasAdd = true;
                                break;
                            }
                        }
                        if (!hasAdd) {
                            dataHasChange = true;
                            userActivities.add(eachParseObject);
                        } else {
                            Log.d(DailyKind.TAG, "CachePolicy Skip object: " + eachParseObject.getObjectId());
                        }
                    }
                    if (dataHasChange) {
                        storyArrayAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogProgressBarView != null) {
                    dialogProgressBarView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void actionButtonSetup() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getString("name") != null) {
            findViewById(R.id.good_deed_content_now_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(DeedContentActivity.this, PostStoryActivity.class);
                    intent.putExtra("idea", idea);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        } else {
            findViewById(R.id.good_deed_content_now_button).setOnClickListener(this.askUserLoginListener);
        }
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

    private void loadIdea() {

        Log.d(DailyKind.TAG, "load Idea with ideaObjectId: " + ideaObjectId);

        if (ideaObjectId == null) return;

        progressBarView.setVisibility(View.VISIBLE);

        ParseQuery<ParseObject> queryIdea = new ParseQuery<ParseObject>("Idea");
        queryIdea.whereEqualTo("objectId", ideaObjectId);
        queryIdea.include("graphicPointer");
        queryIdea.include("categoryPointer");

        queryIdea.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        queryIdea.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject ideaParseObject, ParseException e) {

                findViewById(R.id.good_content_progress_bar).setVisibility(View.GONE);

                if (ideaParseObject != null) {

                    ideaObject = ideaParseObject;

                    idea = new ParseObjectManager(ideaParseObject).getIdea();
                    idea.setCategory(new ParseObjectManager(ideaParseObject.getParseObject("categoryPointer")).getCategory());
                    idea.setGraphic(new ParseObjectManager(ideaParseObject.getParseObject("graphicPointer")).getGraphic());

                    TextView contentTextView = (TextView)findViewById(R.id.idea_content_title_text_view);
                    contentTextView.setText(idea.getName());
                    contentTextView.setMaxLines(99);

                    TextView contentDescriptionTextView = (TextView)findViewById(R.id.idea_content_description_text_view);
                    if (idea.getIdeaDescription() != null && idea.getIdeaDescription().length() > 0) {
                        contentDescriptionTextView.setText(idea.getIdeaDescription());
                        contentDescriptionTextView.setMaxLines(99);
                    } else {
                        contentDescriptionTextView.setVisibility(View.GONE);
                    }

                    TextView categoryTextView = (TextView)findViewById(R.id.idea_content_category_text_view);
                    if (categoryTextView!=null &&
                            idea!=null &&
                            idea.getCategory() != null && idea.getCategory().getName() != null) {
                        categoryTextView.setVisibility(View.VISIBLE);
                        categoryTextView.setText(idea.getCategory().getName());
                    }

                    // Hide for default
                    earnDescribeTextView.setVisibility(View.GONE);

                    if (idea.getGraphic() != null && idea.getGraphic().getParseFileUrl() != null) {
                        contentImageView.setVisibility(View.VISIBLE);
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        contentImageViewLayoutParams = (LinearLayout.LayoutParams)contentImageView.getLayoutParams();
                        contentImageViewLayoutParams.width = displayMetrics.widthPixels;
                        contentImageViewLayoutParams.height = displayMetrics.widthPixels;
                        contentImageView.requestLayout();


                        Log.d(DailyKind.TAG, "Parse File Url: " + idea.getGraphic().getParseFileUrl());

                        Picasso.with(getApplicationContext())
                                .load(idea.getGraphic().getParseFileUrl())
                                .placeholder(R.drawable.card_default)
                                .resize(contentImageViewLayoutParams.width, contentImageViewLayoutParams.height)
                                .into(contentImageView);
                        loadCheckIfEarnedGraphic(ideaParseObject);


                    } else {
                        contentImageView.setVisibility(View.GONE);
                    }
                    // If the done has more than 0
                    if (idea.getDoneCount() > 0) {

                        String completeTimesText = getString(R.string.deed_of_number_of_people_prefix) +
                                getString(R.string.space) +
                                idea.getDoneCount() +
                                getString(R.string.space) +
                                (idea.getDoneCount() > 1 ?
                                        getString(R.string.deed_of_number_of_people_post_times) :
                                        getString(R.string.deed_of_number_of_people_post_time));
                        numberOfPeopleTextView.setText(completeTimesText);
                        storiesButton.setEnabled(true);
                    } else {
                        numberOfPeopleTextView.setText(getString(R.string.deed_content_be_the_first_one));
                        storiesButton.setEnabled(false);
                    }
                }
            }
        });
    }

    private void loadCheckIfEarnedGraphic(final ParseObject ideaParseObject) {
        // Check if user already have this graphic
        // Earn graphic
        ParseQuery<ParseObject> graphicsEarnedQuery = new ParseQuery<ParseObject>("GraphicsEarned");
        graphicsEarnedQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
        graphicsEarnedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        graphicsEarnedQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject parseObject, ParseException e) {
                if (parseObject != null) {
                    ParseQuery<ParseObject> graphicsEarnedQuery = parseObject.getRelation("graphicsEarned").getQuery();
                    graphicsEarnedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
                    graphicsEarnedQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            boolean isUserHaveGraphic = false;
                            if (parseObjects != null && !parseObjects.isEmpty()) {
                                for (ParseObject eachParseObject : parseObjects) {
                                    if (eachParseObject.getObjectId().equals(ideaParseObject.getParseObject("graphicPointer").getObjectId())) {
                                        isUserHaveGraphic = true;
                                        break;
                                    }
                                }
                            }

                            if (!isUserHaveGraphic) {
                                earnDescribeTextView.setText(getString(R.string.deed_content_done_credit));
                                earnDescribeTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                if (e!=null) {
                    Log.e(DailyKind.TAG, "graphicsEarnedQuery exception: " + e.getLocalizedMessage());
                }
            }
        });
    }
}
