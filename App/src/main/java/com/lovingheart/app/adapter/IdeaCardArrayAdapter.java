package com.lovingheart.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.DeedContentActivity;
import com.lovingheart.app.object.Idea;
import com.lovingheart.app.object.IdeaObject;
import com.lovingheart.app.util.parse.ParseObjectManager;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by edward_chiang on 2014/1/17.
 */
public class IdeaCardArrayAdapter extends ArrayAdapter<IdeaObject> {

    /**
     * Constructor
     *
     * @param context  The current context.
     * @param resource The resource ID for a layout file containing a TextView to use when
     *                 instantiating views.
     * @param objects  The objects to represent in the ListView.
     */
    public IdeaCardArrayAdapter(Context context, int resource, List<IdeaObject> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rootView = convertView;
        if (rootView == null) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rootView = inflater.inflate(R.layout.fragment_idea_card, null);
        }

        IdeaObject ideaObject = getItem(position);

        final Idea idea = new ParseObjectManager(ideaObject.getParseObject()).getIdea();
        idea.setGraphic(new ParseObjectManager(ideaObject.getParseObject().getParseObject("graphicPointer")).getGraphic());
        idea.setCategory(new ParseObjectManager(ideaObject.getParseObject().getParseObject("categoryPointer")).getCategory());

        View cardLayout = rootView.findViewById(R.id.home_idea_card_layout);
        cardLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if  (getContext() != null) {
                    Intent intent = new Intent(getContext(), DeedContentActivity.class);
                    intent.putExtra("ideaObjectId", idea.getObjectId());
                    getContext().startActivity(intent);
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
            captionTextView.setText(getContext().getString(ideaObject.getTitleResource()));
        }

        // Title Image
        ImageView titleImageView = (ImageView)rootView.findViewById(R.id.home_random_idea_caption_image_view);
        titleImageView.setImageResource(ideaObject.getTitleImageResource());

        TextView descriptionTextView = (TextView)rootView.findViewById(R.id.idea_content_description_text_view);
        if (descriptionTextView != null && idea.getIdeaDescription() != null && idea.getIdeaDescription() != null) {
            descriptionTextView.setText(idea.getIdeaDescription());
        } else {
            descriptionTextView.setVisibility(View.GONE);
        }

        TextView randomIdeaTextView = (TextView)rootView.findViewById(R.id.idea_content_title_text_view);
        randomIdeaTextView.setText(idea.getName());

        ImageView suggestImageView = (ImageView)rootView.findViewById(R.id.story_content_image_view);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
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
                Picasso.with(getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.card_default)
                        .resize(suggestImageViewLayoutParams.width, suggestImageViewLayoutParams.height)
                        .into(suggestImageView);
            }
        }

        return rootView;
    }


}
