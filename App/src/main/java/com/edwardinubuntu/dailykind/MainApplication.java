package com.edwardinubuntu.dailykind;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.testflightapp.lib.TestFlight;

/**
 * Created by edward_chiang on 2014/1/23.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, ParseSettings.PARSE_API_TOKEN, ParseSettings.PARSE_API_TOKEN_2);
        Crashlytics.start(this);

        TestFlight.takeOff(this, "8d837dea-4632-4f68-8ced-a97b62fe8ac5");
        TestFlight.startSession();
    }

    @Override
    public void onTerminate() {
        TestFlight.endSession();
        super.onTerminate();
    }
}
