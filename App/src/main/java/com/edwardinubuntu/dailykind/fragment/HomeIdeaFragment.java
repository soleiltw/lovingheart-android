package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedContentActivity;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.parse.ParseObject;
import com.squareup.picasso.Picasso;

/**
 * Created by edward_chiang on 2014/1/16.
 */
public class HomeIdeaFragment extends Fragment {

    private ParseObject ideaObject;

    private String title;

    private int titleImageResource;

    public HomeIdeaFragment(ParseObject ideaObject) {
        this.ideaObject = ideaObject;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_idea_card, container, false);

        final Idea idea = new ParseObjectManager(this.ideaObject).getIdea();
        idea.setGraphic(new ParseObjectManager(this.ideaObject.getParseObject("graphicPointer")).getGraphic());
        idea.setCategory(new ParseObjectManager(this.ideaObject.getParseObject("categoryPointer")).getCategory());

        View cardLayout = (View)rootView.findViewById(R.id.home_idea_card_layout);
        cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if  (getActivity() != null) {
                    Intent intent = new Intent(getActivity(), DeedContentActivity.class);
                    intent.putExtra("ideaObjectId", idea.getObjectId());
                    startActivity(intent);
                }
            }
        });

        TextView categoryTextView = (TextView)rootView.findViewById(R.id.idea_content_category_text_view);
        if (categoryTextView!=null &&
                idea!=null &&
                idea.getCategory() != null && idea.getCategory().getName() != null) {
            categoryTextView.setVisibility(View.VISIBLE);
            categoryTextView.setText(idea.getCategory().getName());
        }

        // Title
        TextView captionTextView = (TextView)rootView.findViewById(R.id.home_random_idea_caption_text_view);
        if (captionTextView!=null) {
            captionTextView.setText(getTitle());
        }

        // Title Image
        ImageView titleImageView = (ImageView)rootView.findViewById(R.id.home_random_idea_caption_image_view);
        titleImageView.setImageResource(getTitleImageResource());

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.idea_content_description_text_view);
        if (descriptionTextView != null && idea.getIdeaDescription() != null && idea.getIdeaDescription() != null) {
            descriptionTextView.setText(idea.getIdeaDescription());
        } else {
            descriptionTextView.setVisibility(View.GONE);
        }

        TextView randomIdeaTextView = (TextView)rootView.findViewById(R.id.idea_content_title_text_view);
        randomIdeaTextView.setText(idea.getName());

        ImageView suggestImageView = (ImageView)rootView.findViewById(R.id.story_content_image_view);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LinearLayout.LayoutParams suggestImageViewLayoutParams = (LinearLayout.LayoutParams)suggestImageView.getLayoutParams();
        suggestImageViewLayoutParams.width = displayMetrics.widthPixels;
        suggestImageViewLayoutParams.height = displayMetrics.widthPixels;
        suggestImageView.requestLayout();

        suggestImageView.setImageBitmap(null);
        suggestImageView.setVisibility(View.GONE);
        if (idea.getGraphic() != null && idea.getGraphic().getParseFileUrl() != null) {
            String imageUrl = idea.getGraphic().getParseFileUrl();
            if (imageUrl!=null) {
                suggestImageView.setVisibility(View.VISIBLE);
                Picasso.with(getActivity())
                        .load(imageUrl)
                        .placeholder(R.drawable.card_default)
                        .resize(suggestImageViewLayoutParams.width, suggestImageViewLayoutParams.height)
                        .into(suggestImageView);
            }
        }

        return rootView;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTitleImageResource() {
        return titleImageResource;
    }

    public void setTitleImageResource(int titleImageResource) {
        this.titleImageResource = titleImageResource;
    }
}
