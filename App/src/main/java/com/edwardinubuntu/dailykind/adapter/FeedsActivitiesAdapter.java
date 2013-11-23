package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class FeedsActivitiesAdapter extends UserActivitiesAdapter {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public FeedsActivitiesAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }
}
