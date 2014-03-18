package com.lovingheart.app.util;

import android.content.Context;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.Tracker;

/**
 * Created by edward_chiang on 2014/3/7.
 */
public class AnalyticsManager {

    public static final String GOOGLE_ANALYTICS_TRACKER_ID = "UA-48042327-1";

    private static AnalyticsManager instance;

    private Context context;

    private GoogleAnalytics gaInstance;
    private Tracker gaTracker;

    public static synchronized AnalyticsManager getInstance() {
        if (instance == null) {
            instance = new AnalyticsManager();
        }
        return instance;
    }

    private AnalyticsManager() {
    }

    public void Initialize(Context context) {
        this.context = context;
        /**
         * GA
         */
        if (gaInstance == null && this.context !=null) {
            gaInstance = GoogleAnalytics.getInstance(this.context);
        }
        if (gaTracker == null) {
            gaTracker = gaInstance.getTracker(GOOGLE_ANALYTICS_TRACKER_ID);
        }

        gaInstance.setDefaultTracker(gaTracker);
    }

    public GoogleAnalytics getGaInstance() {
        return gaInstance;
    }

    public Tracker getGaTracker() {
        return gaTracker;
    }
}
