package com.edwardinubuntu.dailykind;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.edwardinubuntu.dailykind.activity.MainActivity;
import com.parse.*;
import com.uservoice.uservoicesdk.Config;
import com.uservoice.uservoicesdk.UserVoice;

import java.util.Locale;

/**
 * Created by edward_chiang on 2014/1/23.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // UserVoice
        Config config = new Config("lovingheart.uservoice.com");
        UserVoice.init(config, this);

        // Parse
        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);

        // Crashlytics
        Crashlytics.start(this);

        ParseUser.enableAutomaticUser();
        ParseACL defaultACL = new ParseACL();
        // Optionally enable public read access while disabling public write access.
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(false);
        ParseACL.setDefaultACL(defaultACL, true);
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getString("name") != null) {

            // Save the current Installation to Parse.
            PushService.setDefaultPushCallback(this, MainActivity.class);

            ParseInstallation parseInstallation = ParseInstallation.getCurrentInstallation();
            parseInstallation.put("language", Locale.getDefault().getLanguage());
            parseInstallation.put("user", ParseUser.getCurrentUser());
            parseInstallation.saveInBackground();

        }
    }
}
