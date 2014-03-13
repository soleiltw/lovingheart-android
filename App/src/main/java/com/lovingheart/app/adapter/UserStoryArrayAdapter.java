package com.lovingheart.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.parse.ParseObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by edward_chiang on 2014/1/31.
 */
public class UserStoryArrayAdapter extends ParseObjectsAdapter {


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

    private static class ViewHolder {
        public ImageView lockedImageView;
        public TextView monthTextView;
        public TextView dayTextView;
        public TextView storyContentTextView;
        public TextView storyAreaTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell_user_stories_by_date, null);

            viewHolder = new ViewHolder();
            viewHolder.lockedImageView = (ImageView)convertView.findViewById(R.id.story_lock_image_view);
            viewHolder.monthTextView = (TextView)convertView.findViewById(R.id.story_date_month_text_view);
            viewHolder.dayTextView = (TextView)convertView.findViewById(R.id.story_date_day_text_view);
            viewHolder.storyContentTextView = (TextView)convertView.findViewById(R.id.story_content_text_view);
            viewHolder.storyAreaTextView = (TextView)convertView.findViewById(R.id.story_area_text_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }
        ParseObject storyObject = getItem(position);

        Date storyCreateAt = storyObject.getCreatedAt();
        Calendar storyCreateAtCal = Calendar.getInstance();
        storyCreateAtCal.setTime(storyCreateAt);

        Locale locale = Locale.getDefault();

        viewHolder.monthTextView.setText(storyCreateAtCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));
        viewHolder.dayTextView.setText(String.valueOf(storyCreateAtCal.get(Calendar.DAY_OF_MONTH)));
        viewHolder.storyContentTextView.setText(storyObject.getString("Content"));
        viewHolder.storyAreaTextView.setText(storyObject.getString("areaName"));

        if (storyObject.getACL()== null || storyObject.getACL().getPublicReadAccess()) {
            viewHolder.lockedImageView.setVisibility(View.GONE);
        } else {
            viewHolder.lockedImageView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
