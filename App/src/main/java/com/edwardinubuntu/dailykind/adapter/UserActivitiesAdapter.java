package com.edwardinubuntu.dailykind.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.listener.LoadMoreListener;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.object.Story;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
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
public class UserActivitiesAdapter extends ArrayAdapter<ParseObject> {

    private LoadMoreListener loadMoreListener;

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
        ParseObjectManager parseObjectManager = new ParseObjectManager(storyObject);
        Story story = parseObjectManager.getStory();

        ImageView storyContentImageView = (ImageView)contentView.findViewById(R.id.story_content_image_view);

        // Check if have graphic
        if (storyObject.getParseObject("graphicPointer") != null) {
            Graphic graphic = new ParseObjectManager(storyObject.getParseObject("graphicPointer")).getGraphic();
            story.setGraphic(graphic);

            if (story.getGraphic() !=null && story.getGraphic().getParseFileUrl() != null) {

                DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                LinearLayout.LayoutParams storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)storyContentImageView.getLayoutParams();
                storyContentImageViewLayoutParams.width = displayMetrics.widthPixels;
                storyContentImageViewLayoutParams.height = displayMetrics.widthPixels;
                storyContentImageView.requestLayout();
                storyContentImageView.setVisibility(View.VISIBLE);

                Picasso.with(getContext())
                    .load(story.getGraphic().getParseFileUrl())
                    .placeholder(R.drawable.card_default)
                    .resize(storyContentImageViewLayoutParams.width, storyContentImageViewLayoutParams.height)
                    .into(storyContentImageView);
            }
        } else {
            storyContentImageView.setVisibility(View.GONE);
        }

        TextView locationAreaNameTextView = (TextView)contentView.findViewById(R.id.user_activity_location_area_name_text_view);
        locationAreaNameTextView.setText(story.getLocationAreaName());

        final ImageView storyTellerImageView = (ImageView)contentView.findViewById(R.id.user_avatar_image_view);
        storyTellerImageView.setImageResource(R.drawable.ic_action_user);
        if (story.getStoryTeller().getParseObject("avatar")!=null) {
            story.getStoryTeller().getParseObject("avatar").fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (parseObject != null) {
                        Picasso.with(getContext())
                                .load(parseObject.getString("imageUrl"))
                                .placeholder(R.drawable.ic_action_user)
                                .transform(new CircleTransform())
                                .into(storyTellerImageView);
                    }
                }
            });

        }

        TextView storyTellerTextView = (TextView)contentView.findViewById(R.id.user_name_text_view);
        storyTellerTextView.setText(story.getStoryTeller().getString("name"));

        TextView storyContentTextView = (TextView)contentView.findViewById(R.id.story_content_text_view);
        storyContentTextView.setText(story.getContent());

        TextView createdAtTextView = (TextView)contentView.findViewById(R.id.created_at_text_view);
        PrettyTime prettyTime = new PrettyTime(new Date());
        createdAtTextView.setText(prettyTime.format(story.getCreatedAt()));

        if (position >= getCount() - 1 && getLoadMoreListener() != null) {
            getLoadMoreListener().notifyLoadMore();
        }

        return contentView;
    }

    public LoadMoreListener getLoadMoreListener() {
        return loadMoreListener;
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }
}
