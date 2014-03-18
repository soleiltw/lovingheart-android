package com.lovingheart.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.util.AnalyticsManager;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.HashMap;

/**
 * Created by edward_chiang on 2014/1/2.
 */
public class UserSignUpFragment extends PlaceholderFragment {

    private EditText emailEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_user_signup, container, false);

        emailEditText = (EditText)rootView.findViewById(R.id.signup_email_edit_Text);

        final EditText userPasswordSignUpEditText = (EditText)rootView.findViewById(R.id.signup_password_edit_Text);

        final EditText userPasswordSignUpConfirmEditText = (EditText)rootView.findViewById(R.id.signup_password_confirm_edit_Text);

        final EditText userNameEditText = (EditText)rootView.findViewById(R.id.signup_username_edit_Text);

        final TextView userErrorTextView = (TextView)rootView.findViewById(R.id.user_signup_exception_text_view);

        rootView.findViewById(R.id.user_signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emailEditText.getText() != null
                        && emailEditText.getText().length() > 0
                        && userNameEditText.getText() != null
                        && userNameEditText.getText().length() > 0
                        && userPasswordSignUpEditText.getText() != null
                        && userPasswordSignUpEditText.getText().length() > 0
                        && userPasswordSignUpConfirmEditText.getText() != null
                        && userPasswordSignUpConfirmEditText.getText().length() > 0
                        && userPasswordSignUpEditText.getText().toString().equals(userPasswordSignUpConfirmEditText.getText().toString())
                        ) {

                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(userPasswordSignUpConfirmEditText.getWindowToken(), 0);

                    userErrorTextView.setText(null);
                    rootView.findViewById(R.id.user_signup_progressBar).setVisibility(View.VISIBLE);

                    ParseUser user = new ParseUser();
                    user.setUsername(emailEditText.getText().toString());
                    user.put("name", userNameEditText.getText().toString());
                    user.setEmail(emailEditText.getText().toString());
                    user.setPassword(userPasswordSignUpConfirmEditText.getText().toString());
                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            rootView.findViewById(R.id.user_signup_progressBar).setVisibility(View.GONE);
                            if  (e == null) {
                                // Hooray! Let them use the app now.
                                putNeedUpdate();
                                getActivity().finish();
                            } else {
                                // Sign up didn't succeed.
                                userErrorTextView.setText(e.getLocalizedMessage());
                            }
                        }
                    });

                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.action_login_form_validate_failure), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailEditText.requestFocus();
    }

    private void putNeedUpdate() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean(DailyKind.NEED_UPDATE_DRAWER, true);
        editor.commit();
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "User Sign Up");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "User Sign Up");
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {

    }
}
