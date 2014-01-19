package com.edwardinubuntu.dailykind.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.parse.*;

import java.util.Arrays;

/**
 * Created by edward_chiang on 2014/1/2.
 */
public class UserLoginFragment extends PlaceholderFragment {

    private EditText userIdLoginEditText;

    private EditText userPasswordLoginEditText;

    private ProgressBar userLoginProgressBar;

    private View userLoginLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_user_login, container, false);

        LoginButton loginButton = (LoginButton)rootView.findViewById(R.id.facebook_auth_button);
        loginButton.setReadPermissions(Arrays.asList("email"));

        userIdLoginEditText = (EditText)rootView.findViewById(R.id.login_email_edit_Text);

        userPasswordLoginEditText = (EditText)rootView.findViewById(R.id.login_password_edit_Text);

        userLoginLayout = rootView.findViewById(R.id.user_login_layout);


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
                    switchLoadingProgress(true);

                    ParseUser.logInInBackground(userIdLoginEditText.getText().toString(),
                            userPasswordLoginEditText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            switchLoadingProgress(false);
                            if (parseUser != null) {
                                // Hooray! The user logged in.
                                Toast.makeText(getActivity(), getResources().getText(R.string.action_username_login_success), Toast.LENGTH_SHORT).show();

                                getActivity().setResult(Activity.RESULT_OK);
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

        this.userLoginProgressBar = (ProgressBar)rootView.findViewById(R.id.user_login_progressBar);

        rootView.findViewById(R.id.facebook_user_login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switchLoadingProgress(true);
                ParseFacebookUtils.logIn(
                        Arrays.asList("email", ParseFacebookUtils.Permissions.Friends.ABOUT_ME)
                        ,getActivity(), new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if (getActivity() != null) {
                            if (user == null) {
                                Log.d(DailyKind.TAG, "Uh oh. The user cancelled the Facebook login.");
                                Toast.makeText(getActivity(), err.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                if (user.isNew()) {
                                    Log.d(DailyKind.TAG, "User signed up and logged in through Facebook!");
                                } else {
                                    Log.d(DailyKind.TAG, "User logged in through Facebook!");
                                }

                                getAdditionalFacebookInfo();


                            }
                            switchLoadingProgress(false);
                        }
                    }
                });
            }
        });

        return rootView;
    }

    private void getAdditionalFacebookInfo() {

        switchLoadingProgress(true);
        Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if (graphUser != null) {

                    ParseUser currentUser = ParseUser.getCurrentUser();

                    if (currentUser.isAuthenticated() &&
                            (!currentUser.has("name") || !currentUser.has("avatar") || !currentUser.has("fbId"))) {
                        currentUser.put("name", graphUser.getName());
                        currentUser.put("fbId", graphUser.getId());

                        if (!currentUser.has("avatar")) {
                            ParseObject graphic = new ParseObject("GraphicImage");
                            graphic.put("imageType", "url");
                            graphic.put("imageUrl", "http://graph.facebook.com/"+graphUser.getId()+"/picture?type=large");
                            try {
                                graphic.save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            currentUser.put("avatar", graphic);
                        }
                        switchLoadingProgress(true);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d(DailyKind.TAG, "Sign up done: " + e.getLocalizedMessage());

                                    getActivity().findViewById(R.id.user_login_layout).setVisibility(View.VISIBLE);
                                } else {
                                    switchLoadingProgress(false);
                                    getActivity().setResult(getActivity().RESULT_OK);
                                    getActivity().finish();
                                }


                            }
                        });
                    } else {
                        switchLoadingProgress(false);
                        getActivity().setResult(getActivity().RESULT_OK);
                        getActivity().finish();
                    }

                }


            }
        });
    }

    private void switchLoadingProgress(boolean isLoading) {
        if (isLoading) {
            userLoginLayout.setVisibility(View.GONE);
            userLoginProgressBar.setVisibility(View.VISIBLE);
        } else {
            userLoginLayout.setVisibility(View.VISIBLE);
            userLoginProgressBar.setVisibility(View.GONE);
        }
    }
}
