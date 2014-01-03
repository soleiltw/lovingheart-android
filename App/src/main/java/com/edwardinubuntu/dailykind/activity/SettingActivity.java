package com.edwardinubuntu.dailykind.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import com.edwardinubuntu.dailykind.R;
import com.parse.ParseUser;

/**
 * Created by edward_chiang on 2014/1/3.
 */
public class SettingActivity extends PreferenceActivity {

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        refreshPreference();
    }

    private void refreshPreference() {
        Preference userLoginPreference = findPreference("setting_user_login");
        if (ParseUser.getCurrentUser() != null) {
            userLoginPreference.setTitle(
                    getString(R.string.setting_user_login_account_title) +
                    getString(R.string.semicolon) +
                    getString(R.string.space)
                    + ParseUser.getCurrentUser().getString("name"));
            userLoginPreference.setSummary(getString(R.string.setting_user_login_account_ask_logout));

            userLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ParseUser.getCurrentUser().logOut();
                    refreshPreference();
                    return true;
                }
            });

        } else {
            // Ask to login
            userLoginPreference.setTitle(
                    getString(R.string.setting_user_login_account_title));
            userLoginPreference.setSummary(getString(R.string.setting_user_login_account_ask_login));
            userLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginIntent);

                    return true;
                }
            });
        }
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
}
