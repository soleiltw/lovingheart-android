package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by edward_chiang on 2014/2/17.
 */
public class PersonalReportAdapter extends ArrayAdapter<String> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public PersonalReportAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;
        // Because we want to update the first view
        if (rootView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
        textView.setText(Html.fromHtml(getItem(position)));
        return rootView;
    }
}
