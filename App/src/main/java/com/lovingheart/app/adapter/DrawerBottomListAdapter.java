package com.lovingheart.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lovingheart.app.R;

/**
 * Created by edward_chiang on 2014/3/11.
 */
public class DrawerBottomListAdapter extends DrawerListAdapter {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public DrawerBottomListAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(android.R.layout.simple_list_item_1, null);

        TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
        textView.setText(getItem(position));

        if (rootView.isSelected()) {
            rootView.setBackgroundColor(getContext().getResources().getColor(R.color.theme_color_4));
            textView.setTextColor(Color.WHITE);
        } else {
            rootView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            textView.setTextColor(Color.BLACK);
        }




        return rootView;
    }
}
