package com.lovingheart.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.UserProfileActivity;
import com.lovingheart.app.object.Graphic;
import com.lovingheart.app.object.Story;
import com.lovingheart.app.object.User;
import com.lovingheart.app.util.CircleTransform;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class StoryArrayAdapter extends ParseObjectsAdapter {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public StoryArrayAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View contentView = convertView;
        if (contentView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            contentView = inflater.inflate(R.layout.cell_stories, null);
        }

        Log.d(DailyKind.TAG, "Story width: " + contentView.getWidth());
        int layoutWidth = contentView.getWidth();

        ParseObject storyObject = getItem(position);
        ParseObjectManager parseObjectManager = new ParseObjectManager(storyObject);
        final Story story = parseObjectManager.getStory();

        ImageView storyContentImageView = (ImageView)contentView.findViewById(R.id.story_content_image_view);

        // Check if have graphic
        if (storyObject.getParseObject("graphicPointer") != null) {
            Graphic graphic = new ParseObjectManager(storyObject.getParseObject("graphicPointer")).getGraphic();
            story.setGraphic(graphic);

            if (story.getGraphic() !=null && story.getGraphic().getParseFileUrl() != null) {

                LinearLayout.LayoutParams storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)storyContentImageView.getLayoutParams();

                if (layoutWidth > 0) {
                    // We make it as screen width
                    storyContentImageViewLayoutParams.width = layoutWidth;
                    storyContentImageViewLayoutParams.height = layoutWidth;

                    notifyDataSetChanged();
                } else {
                    DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                    // We make it as screen width
                    storyContentImageViewLayoutParams.width = displayMetrics.widthPixels;
                    storyContentImageViewLayoutParams.height = displayMetrics.widthPixels;
                }
                storyContentImageView.requestLayout();
                storyContentImageView.setVisibility(View.VISIBLE);

                Picasso.with(getContext())
                    .load(story.getGraphic().getParseFileUrl())
                    .placeholder(R.drawable.card_default)
                    .resize(storyContentImageViewLayoutParams.width, storyContentImageViewLayoutParams.height)
                    .centerCrop()
                    .into(storyContentImageView);
            }
        } else {
            storyContentImageView.setVisibility(View.GONE);
        }

        TextView locationAreaNameTextView = (TextView)contentView.findViewById(R.id.user_activity_location_area_name_text_view);
        if (story.getLocationAreaName() != null) {
            locationAreaNameTextView.setText(
                getContext().getString(R.string.location_area_name_from) + getContext().getString(R.string.space) +
                story.getLocationAreaName());
        }

        User user = new User();
        user.setName(story.getStoryTeller().getString("name"));

        final ImageView storyTellerImageView = (ImageView)contentView.findViewById(R.id.user_avatar_image_view);
        storyTellerImageView.setImageResource(R.drawable.ic_action_user);
        if (story.getStoryTeller() != null
                && story.getStoryTeller().has("avatar") && story.getStoryTeller().getParseObject("avatar")!=null) {
            story.getStoryTeller().getParseObject("avatar").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null) {
                        Picasso.with(getContext())
                                .load(parseObject.getString("imageUrl"))
                                .placeholder(R.drawable.ic_action_user)
                                .transform(new CircleTransform())
                                .into(storyTellerImageView);

                        storyTellerImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent userIntent = new Intent(getContext(), UserProfileActivity.class);
                                userIntent.putExtra("userId", story.getStoryTeller().getObjectId());
                                getContext().startActivity(userIntent);
                            }
                        });
                    }
                }
            });

        }

        TextView storyTellerTextView = (TextView)contentView.findViewById(R.id.user_name_text_view);
        storyTellerTextView.setText(user.getName());

        TextView storyContentTextView = (TextView)contentView.findViewById(R.id.story_content_text_view);
        storyContentTextView.setText(story.getContent());

        TextView createdAtTextView = (TextView)contentView.findViewById(R.id.created_at_text_view);
        PrettyTime prettyTime = new PrettyTime(new Date());
        createdAtTextView.setText(prettyTime.format(story.getCreatedAt()));

        if (position >= getCount() - 1 && getLoadMoreListener() != null && !isLoadMoreEnd() && getCount() >= DailyKind.PARSE_QUERY_LIMIT) {
            getLoadMoreListener().notifyLoadMore();
        }

        return contentView;
    }
}