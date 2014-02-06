package com.edwardinubuntu.dailykind;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class DailyKind {

    public static String TAG = "dailykind";

    public static String PREFERENCE_SUPPORT_ENGLISH = "support_english";

    public static String PREFERENCE_SUPPORT_CHINESE = "support_chinese";

    public static String ACKNOWLEDGEMENT_LINK = "https://dailylovingheart.wordpress.com/acknowledgement/";

    // Every 30 mins
    public static long QUERY_MAX_CACHE_AGE = 60 * 30 * 1000;

    // Every 5 mins
    public static long QUERY_AT_LEAST_CACHE_AGE = 60 * 5 * 1000;
}
