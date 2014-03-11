package com.lovingheart.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by edward_chiang on 2014/3/11.
 */
public class GettingStartedAdapter extends ArrayAdapter<ParseObject> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public GettingStartedAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        ParseObject gettingObject = getItem(position);

        TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
        textView.setText(gettingObject.getString("title"));
        textView.setTextColor(getContext().getResources().getColor(R.color.theme_color_1));

        TextView descriptionView = (TextView)rootView.findViewById(android.R.id.text2);
        descriptionView.setText(gettingObject.getString("description"));
        descriptionView.setTextColor(getContext().getResources().getColor(R.color.lovingheart_gray));

        return rootView;
    }
}
