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
            rootView = inflater.inflate(R.layout.cell_idea_with_done, null);
        }

        Idea idea = getItem(position);

        TextView nameTextView = (TextView)rootView.findViewById(R.id.idea_title_text_view);
        nameTextView.setText(idea.getName());

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.idea_why_text_view);
        descriptionTextView.setText(idea.getIdeaDescription());

        return rootView;
    }
}
