package com.lovingheart.app.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.beardedhen.androidbootstrap.FontAwesomeText;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.object.Review;
import com.lovingheart.app.util.CircleTransform;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;
import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.List;

/**
 * Created by edward_chiang on 2014/1/27.
 */
public class ReviewArrayAdapter extends ArrayAdapter<Review> {
    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public ReviewArrayAdapter(Context context, int resource, List<Review> objects) {
        super(context, resource, objects);

        mDefaultPicDrawable = getContext().getResources().getDrawable(R.drawable.ic_action_user);
    }

    private static class ViewHolder {
        public TextView userNameTextView;
        public FontAwesomeText awesome1Text;
        public FontAwesomeText awesome2Text;
        public FontAwesomeText awesome3Text;
        public FontAwesomeText awesome4Text;
        public FontAwesomeText awesome5Text;
        public ImageView userImageView;
        public TextView reviewTextView;
        public TextView createdAtTextView;
    }

    protected Drawable mDefaultPicDrawable;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.cell_story_review, null);

            viewHolder = new ViewHolder();
            viewHolder.awesome1Text = (FontAwesomeText)convertView.findViewById(R.id.story_review_1_star);
            viewHolder.awesome2Text = (FontAwesomeText)convertView.findViewById(R.id.story_review_2_star);
            viewHolder.awesome3Text = (FontAwesomeText)convertView.findViewById(R.id.story_review_3_star);
            viewHolder.awesome4Text = (FontAwesomeText)convertView.findViewById(R.id.story_review_4_star);
            viewHolder.awesome5Text = (FontAwesomeText)convertView.findViewById(R.id.story_review_5_star);
            viewHolder.userNameTextView = (TextView)convertView.findViewById(R.id.user_name_text_view);
            viewHolder.userImageView = (ImageView)convertView.findViewById(R.id.user_avatar_image_view);
            viewHolder.reviewTextView = (TextView)convertView.findViewById(R.id.story_review_text_view);
            viewHolder.createdAtTextView = (TextView)convertView.findViewById(R.id.story_review_date_text_view);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)convertView.getTag();
            viewHolder.userImageView.setImageDrawable(mDefaultPicDrawable);
        }

        Review review = getItem(position);
        Log.d(DailyKind.TAG, "Rating: " + review.getValue());

        viewHolder.awesome1Text.setVisibility(View.GONE);
        viewHolder.awesome2Text.setVisibility(View.GONE);
        viewHolder.awesome3Text.setVisibility(View.GONE);
        viewHolder.awesome4Text.setVisibility(View.GONE);
        viewHolder.awesome5Text.setVisibility(View.GONE);

        if (review.getValue() >= 1) {
            viewHolder.awesome1Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 2) {
            viewHolder.awesome2Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 3) {
            viewHolder.awesome3Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 4) {
            viewHolder.awesome4Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 5) {
            viewHolder.awesome5Text.setVisibility(View.VISIBLE);
        }

        if (review.getUser() != null) {
            viewHolder.userNameTextView.setText(review.getUser().getName());

            if (review.getUser().getAvatar() != null) {
                review.getUser().getAvatar().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
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
        }

        if (review.getReviewDescription() != null && review.getReviewDescription().length() > 0) {
            viewHolder.reviewTextView.setText(review.getReviewDescription());
            viewHolder.reviewTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.reviewTextView.setVisibility(View.GONE);
        }

        PrettyTime prettyTime = new PrettyTime(new Date());
        viewHolder.createdAtTextView.setText(prettyTime.format(review.getCreatedAt()));

        return convertView;
    }
}
