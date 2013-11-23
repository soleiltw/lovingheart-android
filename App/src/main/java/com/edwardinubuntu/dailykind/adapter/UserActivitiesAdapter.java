package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.parse.ParseObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class UserActivitiesAdapter extends ArrayAdapter<ParseObject> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public UserActivitiesAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View contentView = convertView;
        if (contentView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            contentView = inflater.inflate(R.layout.cell_user_activities, null);
        }

        ParseObject storyObject = getItem(position);

        TextView storyTellerTextView = (TextView)contentView.findViewById(R.id.story_teller_name_text_view);
        storyTellerTextView.setText(storyObject.getParseUser("StoryTeller").getString("username"));

        TextView storyContentTextView = (TextView)contentView.findViewById(R.id.story_content_text_view);
        storyContentTextView.setText(storyObject.getString("Content"));

        TextView createdAtTextView = (TextView)contentView.findViewById(R.id.created_at_text_view);
        PrettyTime prettyTime = new PrettyTime(new Date());
        createdAtTextView.setText(prettyTime.format(storyObject.getCreatedAt()));

        if (storyObject.getString("HelperName") != null && storyObject.getString("HelpedName") != null) {

            TextView helperNameTextView = (TextView)contentView.findViewById(R.id.helper_name_text_view);
            helperNameTextView.setText(storyObject.getString("HelperName"));

            TextView helpedNameTextView = (TextView)contentView.findViewById(R.id.helped_name_text_view);
            helpedNameTextView.setText(storyObject.getString("HelpedName"));
        } else {
            contentView.findViewById(R.id.story_who_help_who_layout).setVisibility(View.GONE);
        }

        return contentView;
    }
}
