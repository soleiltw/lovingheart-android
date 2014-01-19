package com.edwardinubuntu.dailykind.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.fragment.UserLoginFragment;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.parse.*;

/**
 * Created by edward_chiang on 2013/12/25.
 */
public class LoginActivity extends ActionBarActivity {

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.d(DailyKind.TAG, "SessionState: " + state);
            onSessionStateChange(session, state, exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        setContentView(R.layout.activity_login_main);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        ParseFacebookUtils.initialize(getString(R.string.app_id));
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // User Login fragment
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        UserLoginFragment userLoginFragment = new UserLoginFragment();
        fragmentManager.beginTransaction().replace(R.id.login_main_fragment, userLoginFragment).commit();
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
    public void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed()) ) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiHelper.onResume();
    }

    private void onSessionStateChange(Session session, SessionState state,
                                      Exception exception) {
        if (state.isOpened()) {
            Log.d(DailyKind.TAG, "Logged in...");
            makeMeRequest(session, true);
        } else if (state.isClosed()) {
            Log.d(DailyKind.TAG, "Logged out...");
        } else {
            Log.d(DailyKind.TAG, "Unknown state: " + state);
            if (state == SessionState.OPENING) {
                makeMeRequest(session, true);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        uiHelper.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }



    private void makeMeRequest(final Session session, final boolean askLogin) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        Log.d(DailyKind.TAG, "Make request ask to login. " + askLogin);
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {

                        Log.d(DailyKind.TAG, "response: " + response.toString());

                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                Log.d(DailyKind.TAG, "AccessToken: " + session.getAccessToken());
                                Log.d(DailyKind.TAG, "User: " + user.toString());
                                Log.d(DailyKind.TAG, "Name: " + user.getName());

                                if (user.getProperty("email") != null) {
                                    Log.d(DailyKind.TAG, "Email:" + user.getProperty("email").toString());
                                    user.setUsername(user.getProperty("email").toString());
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

        findViewById(R.id.user_login_layout).setVisibility(View.GONE);
        findViewById(R.id.user_login_progressBar).setVisibility(View.VISIBLE);

        ParseUser.logOut();
        ParseUser.logInInBackground(graphUser.getUsername(), graphUser.getId(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {

                if (findViewById(R.id.user_login_progressBar) != null) {
                    findViewById(R.id.user_login_progressBar).setVisibility(View.GONE);
                }

                if (e!= null) {
                    Log.d(DailyKind.TAG, "Parse Exception: " + e.getLocalizedMessage());
                    ParseUser user = new ParseUser();
                    user.setEmail(graphUser.getProperty("email").toString());
                    user.setPassword(graphUser.getId());
                    user.setUsername(graphUser.getProperty("email").toString());
                    user.put("name", graphUser.getUsername());

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

                                findViewById(R.id.user_login_layout).setVisibility(View.VISIBLE);
                            } else {
                                setResult(RESULT_OK);
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "Login Success", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }


}
