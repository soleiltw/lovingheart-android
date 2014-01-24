package com.edwardinubuntu.dailykind.util;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import com.edwardinubuntu.dailykind.DailyKind;

import java.util.Hashtable;

/**
 * Created by edward_chiang on 2014/1/23.
 */
public class Typefaces {

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    public static Typeface get(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e(DailyKind.TAG, "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}
