package com.lovingheart.app.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.object.User;
import com.lovingheart.app.util.CircleTransform;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2014/5/6.
 */
public class UserImpactsAdapter extends ParseObjectsAdapter {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public UserImpactsAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    private static class ViewHolder {
        public TextView userNameTextView;
        public ImageView userImageView;
        public TextView indexOfUserTextView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        View rootView = convertView;
        if (rootView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            rootView = inflater.inflate(R.layout.cell_user, null);

            viewHolder = new ViewHolder();
            viewHolder.userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);
            viewHolder.userImageView = (ImageView)rootView.findViewById(R.id.user_avatar_image_view);
            viewHolder.indexOfUserTextView = (TextView)rootView.findViewById(R.id.index_of_user_text_view);

            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)rootView.getTag();
            Log.d(DailyKind.TAG, "Use a old one at: " + position);
        }

        ParseObject userImpactObject = getItem(position);


        ParseObjectManager parseObjectManager = new ParseObjectManager(userImpactObject.getParseUser("User"));
        User user = parseObjectManager.getUser();

        if (user != null && user.getName() != null && viewHolder.userNameTextView != null) {
            viewHolder.userNameTextView.setText(user.getName());
        }

        viewHolder.indexOfUserTextView.setText( String.valueOf(position + 1) );
        viewHolder.indexOfUserTextView.setVisibility(View.VISIBLE);

        viewHolder.userImageView.setImageResource(R.drawable.ic_action_user);
        if (user.getAvatar() != null) {
            user.getAvatar().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null && parseObject.getString("imageUrl") != null) {
                        Picasso.with(getContext())
                                .load(parseObject.getString("imageUrl"))
                                .placeholder(R.drawable.ic_action_user)
                                .transform(new CircleTransform())
                                .into(viewHolder.userImageView);
                    }
                }
            });
        }

        if (position >= getCount() - 1 && getLoadMoreListener() != null && !isLoadMoreEnd() && getCount() >= DailyKind.PARSE_QUERY_LIMIT) {
            getLoadMoreListener().notifyLoadMore();
        }

        return rootView;
    }
}
