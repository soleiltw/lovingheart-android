package com.edwardinubuntu.dailykind.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
 * Created by edward_chiang on 2014/1/2.
 */
public class UserLoginFragment extends PlaceholderFragment {

    private ImageView userAvatarImageView;

    private EditText userIdLoginEditText;

    private EditText userPasswordLoginEditText;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_user_login, container, false);

        LoginButton loginButton = (LoginButton)rootView.findViewById(R.id.facebook_auth_button);
        loginButton.setReadPermissions(Arrays.asList("email"));

        userAvatarImageView = (ImageView)rootView.findViewById(R.id.login_user_avatar_image_view);

        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().isAuthenticated()) {
            refreshUserProfile();
        }

        userIdLoginEditText = (EditText)rootView.findViewById(R.id.login_email_edit_Text);

        userPasswordLoginEditText = (EditText)rootView.findViewById(R.id.login_password_edit_Text);


        final TextView userErrorTextView = (TextView)rootView.findViewById(R.id.user_login_exception_text_view);

        rootView.findViewById(R.id.user_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userIdLoginEditText.getText() != null
                        && userIdLoginEditText.getText().length() > 0
                        && userPasswordLoginEditText.getText() != null
                        && userPasswordLoginEditText.getText().length() > 0) {

                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userPasswordLoginEditText.getWindowToken(), 0);

                    userErrorTextView.setText(null);
                    rootView.findViewById(R.id.user_login_progressBar).setVisibility(View.VISIBLE);

                    ParseUser.logInInBackground(userIdLoginEditText.getText().toString(),
                            userPasswordLoginEditText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            rootView.findViewById(R.id.user_login_progressBar).setVisibility(View.GONE);
                            if (parseUser != null) {
                                // Hooray! The user logged in.
                                Toast.makeText(getActivity(), getResources().getText(R.string.action_username_login_success), Toast.LENGTH_SHORT).show();
                                getActivity().finish();
                            } else {
                                // Sign up failed.
                                userErrorTextView.setText(e.getLocalizedMessage());
                            }
                        }
                    });

                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.action_login_form_validate_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        rootView.findViewById(R.id.user_signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                UserSignUpFragment userSignUpFragment = new UserSignUpFragment();
                fragmentManager.beginTransaction().replace(R.id.login_main_fragment, userSignUpFragment).commit();
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        uiHelper.onActivityResult(requestCode, resultCode, data);
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
                    Toast.makeText(getActivity(), "Login Success", Toast.LENGTH_SHORT).show();
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
                if (parseUser.has("avatar")) {
                ParseObject graphic = parseUser.getParseObject("avatar");

                if (graphic.getString("imageUrl") != null) {
                    Log.d(DailyKind.TAG, graphic.getString("imageUrl"));
                    Picasso.with(getActivity())
                            .load(graphic.getString("imageUrl"))
                            .transform(new CircleTransform())
                            .into(userAvatarImageView);
                }
                }
            }
        });
    }
}
