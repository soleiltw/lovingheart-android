package com.lovingheart.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.MainActivity;
import com.lovingheart.app.util.CircleTransform;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.squareup.picasso.Picasso;

/**
 * Created by edward_chiang on 2014/2/10.
 */
public class DrawerListAdapter extends ArrayAdapter<String> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public DrawerListAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;

        if (position == MainActivity.VIEW_PAGER_ME_POSITION && ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().has("name")) {

            rootView = inflater.inflate(R.layout.layout_user_image_drawer, null);

            TextView userNameTextView =  (TextView)rootView.findViewById(R.id.user_name_text_view);
            userNameTextView.setText(ParseUser.getCurrentUser().getString("name"));

            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser.has("avatar")) {
                ParseObject avatarObject = parseUser.getParseObject("avatar");

                final ImageView avatarImageView = (ImageView)rootView.findViewById(
                        com.lovingheart.app.R.id.user_avatar_image_view);

                avatarObject.fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject != null && parseObject.getString("imageType").equals("url")) {
                            Picasso.with(getContext())
                                    .load(parseObject.getString("imageUrl"))
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
                    }
                });


            }
        } else  {

            // Because we want to update the first view
            if (rootView == null || position == MainActivity.VIEW_PAGER_ME_POSITION) {
                rootView = inflater.inflate(android.R.layout.simple_list_item_1, null);
            }

            TextView textView = (TextView)rootView.findViewById(android.R.id.text1);
            textView.setText(getItem(position));

            if (rootView.isSelected()) {
                rootView.setBackgroundColor(getContext().getResources().getColor(R.color.theme_color_4));
                textView.setTextColor(Color.WHITE);
            } else {
                rootView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                textView.setTextColor(Color.BLACK);
            }


        }

        return rootView;
    }
}
