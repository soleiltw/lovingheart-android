package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.object.Category;

import java.util.List;

/**
 * Created by edward_chiang on 2013/12/21.
 */
public class CategoryArrayAdapter extends ArrayAdapter<Category> {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public CategoryArrayAdapter(Context context, int resource, List<Category> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(android.R.layout.simple_list_item_1, null);
        }

        Category category = getItem(position);

        TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
        textView.setText(category.getName());
        textView.setTextColor(getContext().getResources().getColor(R.color.theme_color_1));

        return rootView;
    }
}
