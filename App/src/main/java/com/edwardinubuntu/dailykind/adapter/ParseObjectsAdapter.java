package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.widget.ArrayAdapter;
import com.edwardinubuntu.dailykind.listener.LoadMoreListener;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by edward_chiang on 2014/2/23.
 */
public abstract class ParseObjectsAdapter extends ArrayAdapter<ParseObject> {

    private LoadMoreListener loadMoreListener;

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ParseObjectsAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    public LoadMoreListener getLoadMoreListener() {
        return loadMoreListener;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
}
