package com.edwardinubuntu.dailykind.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.edwardinubuntu.dailykind.R;
import com.facebook.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Arrays;

/**
 * Created by edward_chiang on 2014/1/2.
 */
public class UserLoginFragment extends PlaceholderFragment {

    private EditText userIdLoginEditText;

    private EditText userPasswordLoginEditText;

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

        return rootView;
    }
}
