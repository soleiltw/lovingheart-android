package com.lovingheart.app.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.lovingheart.app.object.GettingStarted;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by edward_chiang on 2014/3/11.
 */
public class GettingStartedAdapter extends ArrayAdapter<GettingStarted> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public GettingStartedAdapter(Context context, int resource, List<GettingStarted> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(android.R.layout.simple_list_item_2, null);
        }

        GettingStarted gettingObject = getItem(position);

        TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
        textView.setText(gettingObject.getContentObject().getString("title"));
        textView.setTextColor(getContext().getResources().getColor(R.color.theme_color_2));

        TextView descriptionView = (TextView)rootView.findViewById(android.R.id.text2);
        descriptionView.setText(gettingObject.getContentObject().getString("description"));
        descriptionView.setTextColor(getContext().getResources().getColor(R.color.lovingheart_gray));

        if (gettingObject.getUserLog() != null) {
            ParseObject userLogObject = gettingObject.getUserLog();
            if (userLogObject.has("action") && "done".equalsIgnoreCase(userLogObject.getString("action"))) {
                textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                descriptionView.setPaintFlags(descriptionView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                textView.setTextColor(getContext().getResources().getColor(R.color.lovingheart_gray));
                descriptionView.setTextColor(getContext().getResources().getColor(R.color.lovingheart_gray));
            }
        }

        return rootView;
    }
}
