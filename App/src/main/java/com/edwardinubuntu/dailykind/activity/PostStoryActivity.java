package com.edwardinubuntu.dailykind.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.*;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.beardedhen.androidbootstrap.BootstrapButton;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.ParseSettings;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class PostStoryActivity extends ActionBarActivity {

    protected ProgressDialog storyPostingDialog;

    private Idea idea;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private ProgressBar locationLoadingProgressBar;
    private TextView locationLoadingTextView;
    private ImageView locationAreaImageView;
    protected TextView locationAreaTextView;

    protected EditText contentEditText;

    private Address currentAddress;

    private BootstrapButton submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);

        setContentView(com.edwardinubuntu.dailykind.R.layout.activity_post_story);

        storyPostingDialog = new ProgressDialog(this);
        storyPostingDialog.setMessage(getResources().getString(com.edwardinubuntu.dailykind.R.string.story_upload_progress));
        storyPostingDialog.setIndeterminate(false);
        storyPostingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        storyPostingDialog.setCancelable(false);

        idea = (Idea)getIntent().getSerializableExtra("idea");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ParseUser parseUser = ParseUser.getCurrentUser();
        final ImageView storyTellerImageView = (ImageView)findViewById(com.edwardinubuntu.dailykind.R.id.user_avatar_image_view);

        if (parseUser != null) {
            ParseObject avatarObject = parseUser.getParseObject("avatar");
            if (avatarObject!=null) {
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
            }
        } else {
            // Ask to login
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }


        final ArrayList<String> suggestIdeas = new ArrayList<String>();

        final ArrayAdapter<String> suggestIdeaAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, suggestIdeas);
        AutoCompleteTextView ideaWasTextView = (AutoCompleteTextView)findViewById(R.id.content_idea_from_text_view);
        if (idea!=null) {
            ideaWasTextView.setText(idea.getName());

            ideaWasTextView.setAdapter(suggestIdeaAdapter);
            ideaWasTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        ParseQuery<ParseObject> ideasQuery = new ParseQuery<ParseObject>("Idea");
                        ideasQuery.findInBackground(new FindCallback<ParseObject>() {
                            @Override
                            public void done(List<ParseObject> parseObjects, ParseException e) {
                                if (!parseObjects.isEmpty()) {
                                    suggestIdeas.clear();
                                    for (ParseObject eachParseObject : parseObjects) {
                                        suggestIdeas.add(eachParseObject.getString("Name"));
                                    }
                                    suggestIdeaAdapter.notifyDataSetChanged();
                                }
                            }
                        });
                    }
                }
            });

            ImageView contentImageView = (ImageView)findViewById(R.id.idea_content_image_view);

            contentImageView.setVisibility(View.VISIBLE);
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            LinearLayout.LayoutParams contentImageViewLayoutParams = (LinearLayout.LayoutParams)contentImageView.getLayoutParams();
            contentImageViewLayoutParams.width = displayMetrics.widthPixels / 2;
            contentImageViewLayoutParams.height = displayMetrics.widthPixels / 2;
            contentImageView.requestLayout();

            if (idea.getGraphic()!=null && idea.getGraphic().getParseFileUrl()!=null) {
                Picasso.with(getApplicationContext())
                        .load(idea.getGraphic().getParseFileUrl())
                        .placeholder(R.drawable.card_default)
                        .resize(contentImageViewLayoutParams.width, contentImageViewLayoutParams.height)
                        .into(contentImageView);
            }
        } else {
            findViewById(R.id.content_idea_from_layout).setVisibility(View.GONE);
        }

        findViewById(R.id.post_story_photo_picker_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog askPickerDialog = new Dialog(PostStoryActivity.this);
                askPickerDialog.setContentView(R.layout.layout_photo_picker);
                askPickerDialog.setTitle(getString(R.string.post_story_pick_photo));
                askPickerDialog.findViewById(R.id.post_story_photo_picker_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PostStoryActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                    }
                });
                askPickerDialog.findViewById(R.id.post_story_photo_taken_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PostStoryActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                    }
                });
                askPickerDialog.findViewById(R.id.post_story_photo_gallery_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(PostStoryActivity.this, "Coming soon", Toast.LENGTH_SHORT).show();
                    }
                });
                askPickerDialog.findViewById(R.id.post_story_photo_cancel_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        askPickerDialog.dismiss();
                    }
                });

                askPickerDialog.show();
            }
        });


        locationLoadingProgressBar = (ProgressBar)findViewById(R.id.content_location_progress_bar);
        locationLoadingTextView = (TextView)findViewById(R.id.content_location_progress_text_view);
        locationAreaImageView = (ImageView)findViewById(R.id.content_location_area_image_view);
        locationAreaTextView = (TextView)findViewById(R.id.content_location_area_text_view);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                displayLoadingProgress(false);

                Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                java.util.List<Address> addressList;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    if (!addressList.isEmpty()) {
                        currentAddress = addressList.get(0);

                        Log.d(DailyKind.TAG, "getAdminArea: " + currentAddress.getAdminArea());
                        Log.d(DailyKind.TAG, "getLocality: " + currentAddress.getLocality());

                        locationAreaTextView.setText(getCityNameText(currentAddress.getAdminArea(), currentAddress.getLocality()));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(DailyKind.TAG, e.toString());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(DailyKind.TAG, "Provider: " + provider + " Status: "+status);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d(DailyKind.TAG, "Provider: " + provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e(DailyKind.TAG, "Provider: " + provider);
            }
        };

        requestLocationUpdates();

        submitButton = (BootstrapButton)findViewById(R.id.post_story_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postStory();
            }
        });


        contentEditText = (EditText)findViewById(R.id.content_edit_text);
        contentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                submitButton.setEnabled(contentEditText.getText().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        submitButton.setEnabled(contentEditText.getText().length() > 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        contentEditText.requestFocus();
    }

    private StringBuffer getCityNameText(String adminArea, String locality) {
        StringBuffer cityNamBuffer = new StringBuffer();
        if (adminArea != null && adminArea.length() > 0) {
            cityNamBuffer.append(adminArea);
        }
        if (locality != null && locality.length() > 0) {
            cityNamBuffer.append(getString(R.string.comma) + getString(R.string.space));
            cityNamBuffer.append(locality);
        }
        return cityNamBuffer;
    }

    private void requestLocationUpdates() {

        // TODO Enhance here, GPS vs network
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Update in 5 seconds.
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 100, locationListener);

            displayLoadingProgress(true);
            return;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // Update in 5 seconds.
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 100, locationListener);

            displayLoadingProgress(true);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationManager.removeUpdates(locationListener);
    }

    protected void postStory() {

        if (ParseUser.getCurrentUser() == null) {
            // TODO alert
            return;
        }

        if (contentEditText.getText().length() == 0) {

            new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.post_story_content_need_title))
                    .setMessage(getResources().getString(R.string.post_story_content_need_message))
                    .setPositiveButton(getResources().getString(R.string.go), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int which) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setCancelable(true)
                    .show();

            return;
        }

        final ParseObject parseObject = new ParseObject("Story");

        // TODO Check user has login
        parseObject.put("StoryTeller", ParseUser.getCurrentUser());
        parseObject.put("Content", contentEditText.getText().toString());

        if (idea.getGraphic() != null && idea.getGraphic().getObjectId() != null) {
            ParseQuery<ParseObject> imageQuery = new ParseQuery<ParseObject>("GraphicImage");
            imageQuery.whereEqualTo("objectId", idea.getGraphic().getObjectId());
            try {
                ParseObject imageObject = imageQuery.getFirst();
                parseObject.put("graphicPointer", imageObject);
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e(DailyKind.TAG, e.getLocalizedMessage());
            }

        }

        if (currentAddress != null && locationAreaTextView.getText() != null) {
            ParseGeoPoint parseGeoPoint = new ParseGeoPoint();
            parseGeoPoint.setLatitude(currentAddress.getLatitude());
            parseGeoPoint.setLongitude(currentAddress.getLongitude());
            parseObject.put("geoPoint", parseGeoPoint);
            parseObject.put("areaName", locationAreaTextView.getText());
        }

        if (idea != null) {
            ParseQuery<ParseObject> ideaQuery = new ParseQuery<ParseObject>("Idea");
            ideaQuery.whereEqualTo("objectId", idea.getObjectId());
            storyPostingDialog.show();
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


                    if (idea.getGraphic() != null ) {
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

                    submit(parseObject);

                }
            });
        } else {
            submit(parseObject);
        }
    }

    protected void submit(final ParseObject parseObject) {
        if (!storyPostingDialog.isShowing()) {
            storyPostingDialog.show();
        }
        parseObject.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    if (storyPostingDialog.isShowing()) {
                        storyPostingDialog.dismiss();
                    }
                    Intent storyIntent = new Intent(PostStoryActivity.this, StoryContentActivity.class);
                    storyIntent.putExtra("objectId", parseObject.getObjectId());
                    storyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(storyIntent);
                    finish();
                } else {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                    Toast.makeText(PostStoryActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_story, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void displayLoadingProgress(boolean loading) {
        if (loading) {
            locationLoadingProgressBar.setVisibility(View.VISIBLE);
            locationLoadingTextView.setVisibility(View.VISIBLE);
            locationAreaTextView.setVisibility(View.GONE);
        } else {
            locationLoadingProgressBar.setVisibility(View.GONE);
            locationLoadingTextView.setVisibility(View.GONE);
            locationAreaTextView.setVisibility(View.VISIBLE);
        }
    }
}
