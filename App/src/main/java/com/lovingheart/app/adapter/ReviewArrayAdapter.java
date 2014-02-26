package com.lovingheart.app.adapter;

import android.content.Context;
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = convertView;
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.cell_story_review, null);
        }

        Review review = getItem(position);
        Log.d(DailyKind.TAG, "Rating: " + review.getValue());

        FontAwesomeText awesome1Text = (FontAwesomeText)rootView.findViewById(R.id.story_review_1_star);
        FontAwesomeText awesome2Text = (FontAwesomeText)rootView.findViewById(R.id.story_review_2_star);
        FontAwesomeText awesome3Text = (FontAwesomeText)rootView.findViewById(R.id.story_review_3_star);
        FontAwesomeText awesome4Text = (FontAwesomeText)rootView.findViewById(R.id.story_review_4_star);
        FontAwesomeText awesome5Text = (FontAwesomeText)rootView.findViewById(R.id.story_review_5_star);

        awesome1Text.setVisibility(View.GONE);
        awesome2Text.setVisibility(View.GONE);
        awesome3Text.setVisibility(View.GONE);
        awesome4Text.setVisibility(View.GONE);
        awesome5Text.setVisibility(View.GONE);

        if (review.getValue() >= 1) {
            awesome1Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 2) {

            awesome2Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 3) {

            awesome3Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 4) {
            awesome4Text.setVisibility(View.VISIBLE);
        }
        if (review.getValue() >= 5) {
            awesome5Text.setVisibility(View.VISIBLE);
        }

        if (review.getUser() != null) {
            TextView userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);
            userNameTextView.setText(review.getUser().getName());

            final ImageView userImageView = (ImageView)rootView.findViewById(R.id.user_avatar_image_view);
            userImageView.setImageDrawable(null);
            userImageView.setImageResource(R.drawable.ic_action_user);
            if (review.getUser().getAvatar() != null) {
                review.getUser().getAvatar().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (parseObject != null) {
                            Picasso.with(getContext())
                                    .load(parseObject.getString("imageUrl"))
                                    .placeholder(R.drawable.ic_action_user)
                                    .transform(new CircleTransform())
                                    .into(userImageView);
                        }
                    }
                });

            }
        }

        TextView reviewTextView = (TextView)rootView.findViewById(R.id.story_review_text_view);
        if (review.getReviewDescription() != null && review.getReviewDescription().length() > 0) {
            reviewTextView.setText(review.getReviewDescription());
            reviewTextView.setVisibility(View.VISIBLE);
        } else {
            reviewTextView.setVisibility(View.GONE);
        }

        TextView createdAtTextView = (TextView)rootView.findViewById(R.id.story_review_date_text_view);
        PrettyTime prettyTime = new PrettyTime(new Date());
        createdAtTextView.setText(prettyTime.format(review.getCreatedAt()));

        return rootView;
    }
}
