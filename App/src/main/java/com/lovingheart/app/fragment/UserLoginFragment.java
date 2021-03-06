package com.lovingheart.app.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.*;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by edward_chiang on 2014/1/2.
 */
public class UserLoginFragment extends PlaceholderFragment {

    private EditText userIdLoginEditText;

    private EditText userPasswordLoginEditText;

    private ProgressBar userLoginProgressBar;

    private View userLoginLayout;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

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
                    updateRefreshItem(true);

                    ParseUser.logInInBackground(userIdLoginEditText.getText().toString(),
                            userPasswordLoginEditText.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            updateRefreshItem(false);
                            if (parseUser != null) {
                                // Hooray! The user logged in.
                                Toast.makeText(getActivity(), getResources().getText(R.string.action_username_login_success), Toast.LENGTH_SHORT).show();
                                putNeedUpdate();
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

                updateRefreshItem(true);
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
                            updateRefreshItem(false);
                        }
                    }
                });
            }
        });

        // user_login_terms_of_use_text_view
        View termsOfUseView = rootView.findViewById(R.id.user_login_terms_of_use_text_view);
        termsOfUseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle(getString(R.string.setting_terms_of_use_title));

                WebView webView = new WebView(getActivity());
                webView.loadUrl(DailyKind.TERMS_OF_USE_LINK);
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
        });

        View privacyView = rootView.findViewById(R.id.user_login_privacy_text_view);
        privacyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle(getString(R.string.setting_privacy_policy_title));

                WebView webView = new WebView(getActivity());
                webView.loadUrl(DailyKind.PRIVACY_POLICY_LINK);
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
        });

        return rootView;
    }

    private void getAdditionalFacebookInfo() {

        updateRefreshItem(true);
        Request.executeMeRequestAsync(ParseFacebookUtils.getSession(), new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser graphUser, Response response) {
                if (graphUser != null) {

                    ParseUser currentUser = ParseUser.getCurrentUser();

                    if (currentUser.isAuthenticated() &&
                            (!currentUser.has("name")
                                    || !currentUser.has("email")
                                    || !currentUser.has("avatar")
                                    || !currentUser.has("fbId"))) {
                        currentUser.put("name", graphUser.getName());
                        currentUser.put("fbId", graphUser.getId());
                        currentUser.put("email", graphUser.getProperty("email"));

                        if (!currentUser.has("avatar")) {
                            ParseObject graphic = new ParseObject("GraphicImage");
                            graphic.put("imageType", "url");
                            graphic.put("imageUrl", "https://graph.facebook.com/"+graphUser.getId()+"/picture?type=large");
                            try {
                                graphic.save();
                            } catch (ParseException e1) {
                                e1.printStackTrace();
                            }
                            currentUser.put("avatar", graphic);
                        }
                        updateRefreshItem(true);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.d(DailyKind.TAG, "Sign up done: " + e.getLocalizedMessage());
                                    userLoginLayout.setVisibility(View.VISIBLE);
                                } else {
                                    updateRefreshItem(false);
                                    putNeedUpdate();
                                    getActivity().setResult(getActivity().RESULT_OK);
                                    getActivity().finish();
                                }


                            }
                        });
                    } else {
                        updateRefreshItem(false);

                        putNeedUpdate();

                        getActivity().setResult(getActivity().RESULT_OK);
                        getActivity().finish();
                    }

                }


            }
        });
    }

    private void putNeedUpdate() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(DailyKind.NEED_UPDATE_DRAWER, true);
        editor.commit();
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {
        if (isLoading) {
            userLoginLayout.setVisibility(View.GONE);
            userLoginProgressBar.setVisibility(View.VISIBLE);
        } else {
            userLoginLayout.setVisibility(View.VISIBLE);
            userLoginProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "User Login");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "User Login");
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }
}
