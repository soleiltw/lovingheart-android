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
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.*;
import com.squareup.picasso.Picasso;

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
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject != null) {

                    ParseObjectManager parseObjectManager = new ParseObjectManager(parseObject);
                    idea = parseObjectManager.getIdea();
                    idea.setCategory(parseObjectManager.getCategory());
                    idea.setGraphic(parseObjectManager.getGraphic());

                    TextView contentTextView = (TextView)findViewById(R.id.deed_content_title_text_view);
                    contentTextView.setText(idea.getName());

                    TextView contentDescriptionTextView = (TextView)findViewById(R.id.deed_content_description_text_view);
                    contentDescriptionTextView.setText(idea.getIdeaDescription());

                    if (idea.getGraphic() != null && idea.getGraphic().getParseFileUrl() != null) {
                        contentImageView.setVisibility(View.VISIBLE);
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int minPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
                        contentImageViewLayoutParams = (LinearLayout.LayoutParams)contentImageView.getLayoutParams();
                        contentImageViewLayoutParams.width = minPixels;
                        contentImageViewLayoutParams.height = minPixels;
                        contentImageView.requestLayout();


                        Log.d(DailyKind.TAG, "Parse File Url: " + idea.getGraphic().getParseFileUrl());

                        Picasso.with(getApplicationContext())
                                .load(idea.getGraphic().getParseFileUrl())
                                .placeholder(R.drawable.card_default)
                                .resize(contentImageViewLayoutParams.width, contentImageViewLayoutParams.height)
                                .into(contentImageView);
                    } else {
                        contentImageView.setVisibility(View.GONE);
                    }
                    numberOfPeopleTextView.setText(
                            getString(R.string.deed_of_number_of_people_prefix) +
                            idea.getDoneCount() + getString(R.string.deed_of_number_of_people_post));

                    ParseObject orgParseObject = parseObject.getParseObject("OrganizerPointer");
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
