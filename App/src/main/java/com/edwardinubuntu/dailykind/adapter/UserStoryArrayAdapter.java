package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.parse.ParseObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by edward_chiang on 2014/1/31.
 */
public class UserStoryArrayAdapter extends ArrayAdapter<ParseObject> {


    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public UserStoryArrayAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View contentView = convertView;
        if (contentView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            contentView = inflater.inflate(R.layout.cell_user_stories_by_date, null);
        }
        ParseObject storyObject = getItem(position);

        Date storyCreateAt = storyObject.getCreatedAt();
        Calendar storyCreateAtCal = Calendar.getInstance();
        storyCreateAtCal.setTime(storyCreateAt);

        Locale locale = Locale.getDefault();

        TextView monthTextView = (TextView)contentView.findViewById(R.id.story_date_month_text_view);
        monthTextView.setText(storyCreateAtCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));

        TextView dayTextView = (TextView)contentView.findViewById(R.id.story_date_day_text_view);
        dayTextView.setText(String.valueOf(storyCreateAtCal.get(Calendar.DAY_OF_MONTH)));

        TextView storyContentTextView = (TextView)contentView.findViewById(R.id.story_content_text_view);
        storyContentTextView.setText(storyObject.getString("Content"));

        TextView storyAreaTextView = (TextView)contentView.findViewById(R.id.story_area_text_view);
        storyAreaTextView.setText(storyObject.getString("areaName"));

        return contentView;
    }
}
