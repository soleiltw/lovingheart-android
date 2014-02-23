package com.edwardinubuntu.dailykind;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class DailyKind {

    public static String TAG = "dailykind";

    public static String PREFERENCE_SUPPORT_ENGLISH = "support_english";

    public static String PREFERENCE_SUPPORT_CHINESE = "support_chinese";

    public static String PREFERENCE_PLAYING_SOUND = "playing_sound";

    public static String NEED_UPDATE_DRAWER = "need_update_drawer";

    public static String ACKNOWLEDGEMENT_LINK = "https://dailylovingheart.wordpress.com/acknowledgement/";

    // Every 30 mins
    public static long QUERY_MAX_CACHE_AGE = 60 * 30 * 1000;

    // Every 5 mins
    public static long QUERY_AT_LEAST_CACHE_AGE = 60 * 5 * 1000;

    public static ArrayList<String> getLanguageCollection(Context context) {
        ArrayList<String> languageCollection = new ArrayList<String>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean englishDefaultValue = Locale.getDefault().getLanguage().contains("en");
        boolean supportEnglish = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH, englishDefaultValue);
        if (supportEnglish) {
            languageCollection.add("en");
        }
        boolean chineseDefaultValue = Locale.getDefault().getLanguage().contains("zh");
        boolean supportChinese = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE, chineseDefaultValue);
        if (supportChinese) {
            languageCollection.add("zh");
        }
        return languageCollection;
    }
}
