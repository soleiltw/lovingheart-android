package com.edwardinubuntu.dailykind.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

/**
 * Created by edward_chiang on 2013/12/25.
 */
public class LoginActivity extends ActionBarActivity {

    private ImageView userAvatarImageView;

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.d(DailyKind.TAG, "SessionState: " + state);
            // Check for an open session
            if (session != null && session.isOpened()) {
                // Get the user's data
                makeMeRequest(session, true);
            }
        }
    };

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_login_main);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LoginButton loginButton = (LoginButton)findViewById(R.id.facebook_auth_button);
        loginButton.setReadPermissions(Arrays.asList("email"));

        userAvatarImageView = (ImageView)findViewById(R.id.login_user_avatar_image_view);

        if (ParseUser.getCurrentUser().isAuthenticated()) {
            refreshUserProfile();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
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

    private void makeMeRequest(final Session session, final boolean askLogin) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                Log.d(DailyKind.TAG, "AccessToken: " + session.getAccessToken());
                                Log.d(DailyKind.TAG, "User: " + user.toString());
                                Log.d(DailyKind.TAG, "Name: " + user.getName());

                                if (user.getProperty("email") != null) {
                                    Log.d(DailyKind.TAG, "Email:" + user.getProperty("email").toString());
                                }
                                Log.d(DailyKind.TAG, "Session on completed: " + response.toString());
                                if (askLogin) {
                                    loginParseAccount(user);
                                }

                            }
                        }
                        if (response.getError() != null) {
                            // Handle errors, will do so later.
                        }
                    }
                });
        request.executeAsync();
    }

    private void loginParseAccount(final GraphUser graphUser) {
        ParseUser.logOut();
        ParseUser.logInInBackground(graphUser.getUsername(), graphUser.getId(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e!= null) {
                    Log.d(DailyKind.TAG, "Parse Exception: " + e.getLocalizedMessage());
                    ParseUser user = new ParseUser();
                    user.setEmail(graphUser.getProperty("email").toString());
                    user.setPassword(graphUser.getId());
                    user.setUsername(graphUser.getUsername());

                    ParseObject graphic = new ParseObject("GraphicImage");
                    graphic.put("imageUrl", "http://graph.facebook.com/"+graphUser.getId()+"/picture?type=medium");
                    try {
                        graphic.save();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                    user.put("avatar", graphic);
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.d(DailyKind.TAG, "Sign up done: " + e.getLocalizedMessage());
                            } else {
                                refreshUserProfile();
                            }

                        }
                    });
                } else {
                    Toast.makeText(getApplication(), "Login Success", Toast.LENGTH_SHORT).show();
                    refreshUserProfile();
                }
            }
        });
    }

    private void refreshUserProfile() {
        ParseQuery<ParseUser> parseUserQuery = new ParseQuery<ParseUser>(ParseUser.class);
        parseUserQuery.include("avatar");
        parseUserQuery.whereEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        parseUserQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                ParseObject graphic = parseUser.getParseObject("avatar");

                if (graphic.getString("imageUrl") != null) {
                    Log.d(DailyKind.TAG, graphic.getString("imageUrl"));
                    Picasso.with(getApplicationContext())
                            .load(graphic.getString("imageUrl"))
                            .transform(new CircleTransform())
                            .into(userAvatarImageView);
                }
            }
        });
    }
}
