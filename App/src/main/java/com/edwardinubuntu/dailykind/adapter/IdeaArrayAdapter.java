package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Idea;

import java.util.List;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class IdeaArrayAdapter extends ArrayAdapter<Idea> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public IdeaArrayAdapter(Context context, int resource, List<Idea> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        Idea idea = getItem(position);

        TextView nameTextView = (TextView)rootView.findViewById(android.R.id.text1);
        nameTextView.setText(idea.getName());
        nameTextView.setTextColor(getContext().getResources().getColor(R.color.theme_color_1));

        TextView descriptionTextView = (TextView)rootView.findViewById(android.R.id.text2);
        descriptionTextView.setText(idea.getIdeaDescription());
        descriptionTextView.setTextColor(getContext().getResources().getColor(R.color.theme_color_2));

        return rootView;
    }
}
