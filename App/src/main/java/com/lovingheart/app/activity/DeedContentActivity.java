package com.lovingheart.app.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.devsmart.android.ui.HorizontalListView;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.adapter.OrgListArrayAdapter;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.util.CheckUserLoginUtil;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
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

    private View progressBarView;

    private List<ParseObject> userActivities;

    private BootstrapButton storiesButton;

    private HorizontalListView orgHorizontalListView;

    private List<ParseObject> orgNameList;

    private OrgListArrayAdapter orgArrayAdapter;

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

        orgNameList = new ArrayList<ParseObject>();

        orgArrayAdapter = new OrgListArrayAdapter(DeedContentActivity.this, R.layout.layout_org_image_view, orgNameList);
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
                // Open intent
                Intent storiesIntent = new Intent(DeedContentActivity.this, StoriesCategoryActivity.class);
                storiesIntent.putExtra("ideaObjectId", ideaObjectId);
                startActivity(storiesIntent);
            }
        });

        contentImageView = (ImageView)findViewById(R.id.story_content_image_view);

        earnDescribeTextView = (TextView)findViewById(R.id.deed_content_earn_description_text_view);

        progressBarView = findViewById(R.id.good_content_progress_bar);

        orgHorizontalListView = (HorizontalListView)findViewById(R.id.idea_content_org_horizontal_list_view);
        orgHorizontalListView.setAdapter(orgArrayAdapter);

        findViewById(R.id.idea_content_org_layout).setVisibility(View.GONE);

        loadIdea();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.story_content, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_share: {

                String imageUrl = new String();
                if (idea.getGraphic() != null) {

                    if ("url".equalsIgnoreCase(idea.getGraphic().getFileType())) {
                        imageUrl = idea.getGraphic().getImageUrl();
                    } else if ("file".equalsIgnoreCase(idea.getGraphic().getFileType())) {
                        imageUrl = idea.getGraphic().getParseFileUrl();
                    }
                }

                final Bundle facebookShareParams = new Bundle();
                facebookShareParams.putString("name",
                        idea.getCategory().getName() +
                        getString(R.string.space) +
                        getString(R.string.facebook_share_name_idea_card));
                facebookShareParams.putString("caption", idea.getName());
                if (idea.getIdeaDescription() != null) {
                    facebookShareParams.putString("description", idea.getIdeaDescription());
                } else {
                    facebookShareParams.putString("description", "From LovingHeart for Android.");
                }

                facebookShareParams.putString("link", "http://tw.lovingheartapp.com/deed/"+idea.getObjectId());
                facebookShareParams.putString("picture", imageUrl);

                Session.openActiveSession(DeedContentActivity.this, true, new Session.StatusCallback() {
                    @Override
                    public void call(Session session, SessionState state, Exception exception) {

                        if (session.isOpened()) {

                            // Invoke the dialog
                            WebDialog feedDialog = (
                                    new WebDialog.FeedDialogBuilder(DeedContentActivity.this,
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
                                                    Toast.makeText(DeedContentActivity.this,
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
                            if (!(DeedContentActivity.this).isFinishing()) {
                                feedDialog.show();
                            }
                        }
                    }
                });



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

        queryIdea.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        queryIdea.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        queryIdea.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject ideaParseObject, ParseException e) {

                findViewById(R.id.good_content_progress_bar).setVisibility(View.GONE);

                if (ideaParseObject != null) {

                    if (ideaParseObject.has("orgRelation")) {
                        ParseQuery<ParseObject> orgParseQuery = ideaParseObject.getRelation("orgRelation").getQuery();
                        orgParseQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                        orgParseQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
                        orgParseQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                if (parseObjects != null) {

                                    if (parseObjects.isEmpty()) {
                                        // Empty relation
                                        Log.d(DailyKind.TAG, "Empty org relation.");
                                        findViewById(R.id.idea_content_org_layout).setVisibility(View.GONE);
                                    } else {
                                        findViewById(R.id.idea_content_org_layout).setVisibility(View.VISIBLE);
                                    }

                                    orgNameList.clear();
                                    for (ParseObject orgObject : parseObjects) {
                                        Log.d(DailyKind.TAG, "orgObject id: " + orgObject.getObjectId());
                                        orgNameList.add(orgObject);
                                        orgArrayAdapter.notifyDataSetChanged();
                                        orgHorizontalListView.requestLayout();
                                        orgHorizontalListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                ParseObject orgObject = orgNameList.get(position);

                                                if (orgObject.has("webUrl")) {
                                                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(DeedContentActivity.this);
                                                    alertBuilder.setTitle(orgObject.getString("name"));

                                                    WebView webView = new WebView(DeedContentActivity.this);
                                                    webView.loadUrl(orgObject.getString("webUrl"));
                                                    webView.setWebViewClient(new WebViewClient(){
                                                        /**
                                                         * Give the host application a chance to take over the control when a new
                                                         * url is about to be loaded in the current WebView. If WebViewClient is not
                                                         * provided, by default WebView will ask Activity Manager to choose the
                                                         * proper handler for the url. If WebViewClient is provided, return true
                                                         * means the host application handles the url, while return false means the
                                                         * current WebView handles the url.
                                                         * This method is not called for requests using the POST "method".
                                                         *
                                                         * @param view The WebView that is initiating the callback.
                                                         * @param url  The url to be loaded.
                                                         * @return True if the host application wants to leave the current WebView
                                                         * and handle the url itself, otherwise return false.
                                                         */
                                                        @Override
                                                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                            view.loadUrl(url);
                                                            return true;
                                                        }
                                                    });

                                                    alertBuilder.setView(webView);
                                                    alertBuilder.setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    alertBuilder.show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });

                    }

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

                        contentImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent imageIntent = new Intent(DeedContentActivity.this, ImageViewActivity.class);
                                imageIntent.putExtra("imageUrl", idea.getGraphic().getParseFileUrl());
                                startActivity(imageIntent);
                            }
                        });

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

                    if (ideaParseObject.has("webUrl")) {
                        TextView actionUrlView = (TextView)findViewById(R.id.story_content_action_view);
                        actionUrlView.setVisibility(View.VISIBLE);
                        actionUrlView.setText(ideaParseObject.getString("webUrlActionCall"));
                        actionUrlView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                HashMap<String, String> gaParams = new HashMap<String, String>();
                                gaParams.put(Fields.SCREEN_NAME, "Deed Content");
                                gaParams.put(Fields.EVENT_ACTION, "Web Url Click");
                                gaParams.put(Fields.EVENT_CATEGORY, "Deed Content/" + idea.getName());
                                gaParams.put(Fields.EVENT_LABEL, ideaParseObject.getString("webUrl"));
                                AnalyticsManager.getInstance().getGaTracker().send(gaParams);

                                Intent webUrlActivityIntent = new Intent(DeedContentActivity.this, WebViewActivity.class);
                                webUrlActivityIntent.putExtra("webUrl", ideaParseObject.getString("webUrl"));
                                startActivity(webUrlActivityIntent);
                            }
                        });
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

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }
}
