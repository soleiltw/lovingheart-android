package com.edwardinubuntu.dailykind.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.view.MenuItem;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.facebook.Session;
import com.parse.ParseUser;

import java.util.Locale;

/**
 * Created by edward_chiang on 2014/1/3.
 */
public class SettingActivity extends PreferenceActivity {

    private SharedPreferences preferences;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        addPreferencesFromResource(R.xml.settings);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshPreference();
    }

    private void refreshPreference() {
        Preference userLoginPreference = findPreference("setting_user_login");
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getString("name") != null) {
            userLoginPreference.setTitle(
                    getString(R.string.setting_user_login_account_title) +
                    getString(R.string.semicolon) +
                    getString(R.string.space)
                    + ParseUser.getCurrentUser().getString("name"));
            userLoginPreference.setSummary(getString(R.string.setting_user_login_account_ask_logout));

            userLoginPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ParseUser.logOut();

                    Session session = Session.getActiveSession();
                    if (session !=null ){
                        if (!session.isClosed()) {
                            session.closeAndClearTokenInformation();
                            //clear your preferences if saved
                        }
                    } else {
                        session = new Session(getApplicationContext());
                        Session.setActiveSession(session);

                        session.closeAndClearTokenInformation();
                        //clear your preferences if saved
                    }

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

        Preference settingPreference = findPreference("setting_acknowledgement");
        settingPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent webIntent = new Intent(getApplicationContext(), WebViewActivity.class);
                webIntent.putExtra("webUrl", DailyKind.ACKNOWLEDGEMENT_LINK);
                startActivity(webIntent);

                return true;
            }
        });

        boolean englishDefaultValue = Locale.getDefault().getLanguage().contains("en");
        boolean preferEnglishSaved = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH, englishDefaultValue);

        boolean chineseDefaultValue = Locale.getDefault().getLanguage().contains("zh");
        boolean preferChineseSaved = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE, chineseDefaultValue);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            SwitchPreference englishSwitchPreference = (SwitchPreference)findPreference("setting_support_english");
            englishSwitchPreference.setDefaultValue(preferEnglishSaved);
            englishSwitchPreference.setChecked(preferEnglishSaved);
            englishSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = preference.getEditor();
                    editor.putBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH,
                            Boolean.parseBoolean(newValue.toString()));
                    editor.commit();
                    return true;
                }
            });

            SwitchPreference chineseSwitchPreference = (SwitchPreference)findPreference("setting_support_chinese");
            chineseSwitchPreference.setDefaultValue(preferChineseSaved);
            chineseSwitchPreference.setChecked(preferChineseSaved);
            chineseSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = preference.getEditor();
                    editor.putBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE,
                            Boolean.parseBoolean(newValue.toString()));
                    editor.commit();
                    return true;
                }
            });
        } else {
            CheckBoxPreference englishSwitchPreference = (CheckBoxPreference)findPreference("setting_support_english");
            englishSwitchPreference.setDefaultValue(preferEnglishSaved);
            englishSwitchPreference.setChecked(preferEnglishSaved);
            englishSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = preference.getEditor();
                    editor.putBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH,
                            Boolean.parseBoolean(newValue.toString()));
                    editor.commit();
                    return true;
                }
            });

            CheckBoxPreference chineseSwitchPreference = (CheckBoxPreference)findPreference("setting_support_chinese");
            chineseSwitchPreference.setDefaultValue(preferChineseSaved);
            chineseSwitchPreference.setChecked(preferChineseSaved);
            chineseSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences.Editor editor = preference.getEditor();
                    editor.putBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE,
                            Boolean.parseBoolean(newValue.toString()));
                    editor.commit();
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
