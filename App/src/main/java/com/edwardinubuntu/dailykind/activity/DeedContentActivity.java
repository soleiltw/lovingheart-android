package com.edwardinubuntu.dailykind.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class DeedContentActivity extends ActionBarActivity {

    private String ideaObjectId;

    private ImageView contentImageView;

    private LinearLayout.LayoutParams contentImageViewLayoutParams;

    private TextView numberOfPeopleTextView;

    private ImageView orgImageView;
    private TextView orgTitleTextView;

    private Idea idea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_good_deed_content);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        ideaObjectId = getIntent().getStringExtra("ideaObjectId");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        numberOfPeopleTextView = (TextView)findViewById(R.id.number_of_people_involved_text_view);

        findViewById(R.id.good_deed_content_now_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog confirmDialog = new AlertDialog.Builder(DeedContentActivity.this)
                        .setTitle("Deed")
                        .setMessage("I have done this.")
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                Intent intent = new Intent(getApplicationContext(), PostStoryActivity.class);
                                intent.putExtra("idea", idea);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false).create();
                confirmDialog.show();
            }
        });

        contentImageView = (ImageView)findViewById(R.id.deed_content_image_view);

        orgImageView = (ImageView)findViewById(R.id.good_deed_content_org_avatar_image_view);
        orgTitleTextView = (TextView)findViewById(R.id.good_deed_content_org_text_view);

        loadIdea();
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

        ParseQuery<ParseObject> queryIdea = new ParseQuery<ParseObject>("Idea");
        queryIdea.whereEqualTo("objectId", ideaObjectId);
        queryIdea.include("graphicPointer");
        queryIdea.include("categoryPointer");
        queryIdea.include("OrganizerPointer");
        queryIdea.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject ideaParseObject, ParseException e) {
                if (ideaParseObject != null) {

                    idea = new ParseObjectManager(ideaParseObject).getIdea();
                    idea.setCategory(new ParseObjectManager(ideaParseObject.getParseObject("categoryPointer")).getCategory());
                    idea.setGraphic(new ParseObjectManager(ideaParseObject.getParseObject("graphicPointer")).getGraphic());

                    TextView contentTextView = (TextView)findViewById(R.id.deed_content_title_text_view);
                    contentTextView.setText(idea.getName());

                    TextView contentDescriptionTextView = (TextView)findViewById(R.id.deed_content_description_text_view);
                    if (idea.getIdeaDescription() != null && idea.getIdeaDescription().length() > 0) {
                        contentDescriptionTextView.setText(idea.getIdeaDescription());
                    } else {
                        contentDescriptionTextView.setVisibility(View.GONE);
                    }

                    final TextView earnDescribeTextView = (TextView)findViewById(R.id.deed_content_earn_description_text_view);
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

                        // Check if user already have this graphic
                        // Earn graphic
                        ParseQuery<ParseObject> graphicsEarnedQuery = new ParseQuery<ParseObject>("GraphicsEarned");
                        graphicsEarnedQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
                        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(final ParseObject parseObject, ParseException e) {
                                if (parseObject != null) {
                                    ParseQuery<ParseObject> graphicsEarnedQuery = parseObject.getRelation("graphicsEarned").getQuery();
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
                                            } else {
                                                Toast.makeText(getApplicationContext(), "You have earned the graphic already! Great!", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });



                                }
                                if (e!=null) {
                                    Log.e(DailyKind.TAG, "graphicsEarnedQuery exception: " + e.getLocalizedMessage());
                                }
                            }
                        });
                    } else {
                        contentImageView.setVisibility(View.GONE);
                    }
                    // If the done has more than 0
                    if (idea.getDoneCount() > 0) {
                        numberOfPeopleTextView.setText(
                                getString(R.string.deed_of_number_of_people_prefix) +
                                        idea.getDoneCount() + getString(R.string.deed_of_number_of_people_post));
                    } else {
                        numberOfPeopleTextView.setText(getString(R.string.deed_content_be_the_first_one));
                    }

                    ParseObject orgParseObject = ideaParseObject.getParseObject("OrganizerPointer");
                    if (orgParseObject != null) {

                        orgTitleTextView.setText(orgParseObject.getString("name"));

                        ParseObject graphicObject = orgParseObject.getParseObject("graphicPointer");
                        graphicObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject parseObject, ParseException e) {
                                if  (parseObject != null) {
                                    Picasso.with(getApplicationContext())
                                            .load(parseObject.getParseFile("imageFile").getUrl())
                                            .placeholder(R.drawable.ic_action_user)
                                            .into(orgImageView);
                                }
                            }
                        });
                    }

                }
            }
        });
    }
}
