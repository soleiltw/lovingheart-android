package com.lovingheart.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class DailyKind {

    public static String TAG = "lovingheart";

    public static String PREFERENCE_SUPPORT_ENGLISH = "support_english";

    public static String PREFERENCE_SUPPORT_CHINESE = "support_chinese";

    public static String PREFERENCE_PLAYING_SOUND = "playing_sound";

    public static String NEED_UPDATE_DRAWER = "need_update_drawer";

    public static String ACKNOWLEDGEMENT_LINK = "http://support.lovingheartapp.com/knowledgebase/articles/333115-acknowledgement#anchor";

    public static String PRIVACY_POLICY_LINK = "http://support.lovingheartapp.com/knowledgebase/articles/333113-privacy-policy#anchor";

    public static String TERMS_OF_USE_LINK = "http://support.lovingheartapp.com/knowledgebase/articles/334311-terms-and-conditions-of-use#anchor";

    public static final String PARSE_PREMIUM_NOCHECK = "nocheck";

    // Every 30 mins
    public static long QUERY_MAX_CACHE_AGE = 60 * 30 * 1000;

    // Every 5 mins
    public static long QUERY_AT_LEAST_CACHE_AGE = 60 * 5 * 1000;

    public static final int PARSE_QUERY_LIMIT = 10;

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
