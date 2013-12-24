package com.edwardinubuntu.dailykind.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoriesActivity;
import com.edwardinubuntu.dailykind.object.Category;
import com.edwardinubuntu.dailykind.object.Idea;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.squareup.picasso.Picasso;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class HomeFragment extends PlaceholderFragment {

    private Button categoriesDeedButton;

    private TextView randomIdeaTextView;

    private ProgressBar randomLoadingProgressBar;

    private ImageView suggestImageView;

    private LinearLayout.LayoutParams suggestImageViewLayoutParams;

    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryRandomIdea();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        randomIdeaTextView = (TextView)rootView.findViewById(R.id.home_random_idea_text_view);

        suggestImageView = (ImageView)rootView.findViewById(R.id.home_random_suggest_image_view);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int minPixels = Math.min(displayMetrics.widthPixels, displayMetrics.heightPixels);
        suggestImageViewLayoutParams = (LinearLayout.LayoutParams)suggestImageView.getLayoutParams();
        suggestImageViewLayoutParams.width = minPixels;
        suggestImageViewLayoutParams.height = minPixels;
        suggestImageView.requestLayout();

        randomLoadingProgressBar = (ProgressBar)rootView.findViewById(R.id.home_good_deed_random_progressBar);

        Button randomDeedButton = (Button)rootView.findViewById(R.id.home_good_deed_random_of_button);
        randomDeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                queryRandomIdea();
            }
        });

        categoriesDeedButton = (Button)rootView.findViewById(R.id.home_good_deed_categories_of_button);
        categoriesDeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeedCategoriesActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void queryRandomIdea() {

        randomLoadingProgressBar.setVisibility(View.VISIBLE);

        final ParseQuery<ParseObject> randomIdeaQuery = new ParseQuery<ParseObject>("Idea");
        randomIdeaQuery.addDescendingOrder("Index");
        randomIdeaQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject != null) {
                    int maxIndex = parseObject.getInt("Index");
                    int randomIndex = (int)(Math.random() * maxIndex);

                    randomIdeaQuery.include("categoryPointer");
                    randomIdeaQuery.include("graphicPointer");
                    randomIdeaQuery.whereEqualTo("Index", randomIndex);
                    randomIdeaQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            if (parseObject!= null) {
                                Idea idea = new Idea();

                                idea.setName(parseObject.getString("Name"));
                                idea.setIdeaDescription(parseObject.getString("Description"));

                                Category category = new Category();
                                ParseObject categoryObject = parseObject.getParseObject("categoryPointer");
                                category.setObjectId(categoryObject.getObjectId());
                                category.setName(categoryObject.getString("Name"));

                                idea.setCategory(category);

                                randomIdeaTextView.setText(idea.getName());

                                ParseObject graphicObject = parseObject.getParseObject("graphicPointer");
                                if (graphicObject != null && graphicObject.getParseFile("imageFile") != null) {
                                    String imageUrl = graphicObject.getParseFile("imageFile").getUrl();

                                    suggestImageView.setImageBitmap(null);
                                    if (imageUrl!=null) {
                                        Picasso.with(getActivity())
                                                .load(imageUrl)
                                                .resize(suggestImageViewLayoutParams.width, suggestImageViewLayoutParams.height)
                                                .into(suggestImageView);
                                    }
                                }

                            }
                            randomLoadingProgressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    randomLoadingProgressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
