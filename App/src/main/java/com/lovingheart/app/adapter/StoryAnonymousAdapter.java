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
import com.lovingheart.app.activity.DeedContentActivity;
import com.lovingheart.app.object.Graphic;
import com.lovingheart.app.object.Story;
import com.lovingheart.app.object.User;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2014/3/22.
 */
public class StoryAnonymousAdapter extends StoryArrayAdapter {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public StoryAnonymousAdapter(Context context, int resource, List<ParseObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ParseObject storyObject = getItem(position);
        ParseObjectManager parseObjectManager = new ParseObjectManager(storyObject);
        final Story story = parseObjectManager.getStory();
        User user = new User();
        if  (story.getStoryTeller() != null) {
            user.setName(story.getStoryTeller().getString("name"));
        }

        if (storyObject.getParseObject("graphicPointer") != null) {
            Graphic graphic = new ParseObjectManager(storyObject.getParseObject("graphicPointer")).getGraphic();
            story.setGraphic(graphic);
        }


        final ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cell_stories, null);

            viewHolder = new ViewHolder();
            viewHolder.storyContentImageView = (ImageView)convertView.findViewById(R.id.story_content_image_view);
            viewHolder.locationAreaNameTextView = (TextView)convertView.findViewById(R.id.user_activity_location_area_name_text_view);
            viewHolder.storyTellerImageView = (ImageView)convertView.findViewById(R.id.user_avatar_image_view);
            viewHolder.storyTellerTextView = (TextView)convertView.findViewById(R.id.user_name_text_view);
            viewHolder.storyContentTextView = (TextView)convertView.findViewById(R.id.story_content_text_view);
            viewHolder.createdAtTextView = (TextView)convertView.findViewById(R.id.created_at_text_view);
            viewHolder.storyLockedImageView = (ImageView)convertView.findViewById(R.id.story_lock_image_view);
            viewHolder.ideaViewGroup = convertView.findViewById(R.id.story_idea_group_layout);
            viewHolder.inspiredFromTextView = (TextView)viewHolder.ideaViewGroup.findViewById(R.id.me_stories_last_share_inspired_from_text_view);
            viewHolder.categoryTextView = (TextView)viewHolder.ideaViewGroup.findViewById(R.id.story_content_category_text_view);

            Log.d(DailyKind.TAG, "Story width: " + convertView.getWidth());

            Log.d(DailyKind.TAG, "ListView-rec Create new one at " + position);
            int layoutWidth = convertView.getWidth();
            LinearLayout.LayoutParams storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)viewHolder.storyContentImageView.getLayoutParams();

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
            viewHolder.storyContentImageView.requestLayout();
            viewHolder.storyContentImageView.setVisibility(View.VISIBLE);

            loadStoryContentImageView(viewHolder, story, storyContentImageViewLayoutParams);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();

            Log.d(DailyKind.TAG, "ListView-rec Reuse one at " + position);

            LinearLayout.LayoutParams storyContentImageViewLayoutParams = (LinearLayout.LayoutParams)viewHolder.storyContentImageView.getLayoutParams();
            loadStoryContentImageView(viewHolder, story, storyContentImageViewLayoutParams);
        }

        if (storyObject.has("ideaPointer")) {
            final ParseObject ideaObject = storyObject.getParseObject("ideaPointer");

            viewHolder.inspiredFromTextView.setText(ideaObject.getString("Name"));
            if (ideaObject.has("categoryPointer")) {
                ParseObject categoryObject = ideaObject.getParseObject("categoryPointer");
                categoryObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(final ParseObject categoryObject, ParseException e) {

                        if (categoryObject!= null && categoryObject.has("Name")) {
                            viewHolder.categoryTextView.setText(categoryObject.getString("Name"));

                            viewHolder.categoryTextView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent categoryIdeaIntent = new Intent(getContext(), DeedContentActivity.class);
                                    categoryIdeaIntent.putExtra("ideaObjectId", ideaObject.getObjectId());
                                    getContext().startActivity(categoryIdeaIntent);
                                }
                            });
                        }
                    }
                });
            }
            viewHolder.ideaViewGroup.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ideaViewGroup.setVisibility(View.GONE);
        }

        if (story.getLocationAreaName() != null) {
            viewHolder.locationAreaNameTextView.setText(
                    getContext().getString(R.string.location_area_name_from) + getContext().getString(R.string.space) +
                            story.getLocationAreaName()
            );
        }

        viewHolder.storyTellerImageView.setImageResource(R.drawable.ic_action_emo_cool);

        viewHolder.storyTellerTextView.setText(getContext().getString(R.string.story_teller_anonymous));
        viewHolder.storyContentTextView.setText(story.getContent());

        PrettyTime prettyTime = new PrettyTime(new Date());
        viewHolder.createdAtTextView.setText(prettyTime.format(story.getCreatedAt()));

        if (storyObject.getACL() != null && !storyObject.getACL().getPublicReadAccess()) {
            viewHolder.storyLockedImageView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.storyLockedImageView.setVisibility(View.GONE);
        }

        if (position >= getCount() - 1 && getLoadMoreListener() != null && !isLoadMoreEnd() && getCount() >= DailyKind.PARSE_QUERY_LIMIT) {
            getLoadMoreListener().notifyLoadMore();
        }

        return convertView;
    }

}
