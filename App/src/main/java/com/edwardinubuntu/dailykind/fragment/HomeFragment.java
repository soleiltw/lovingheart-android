package com.edwardinubuntu.dailykind.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoriesActivity;
import com.edwardinubuntu.dailykind.activity.DeedContentActivity;
import com.edwardinubuntu.dailykind.listener.ImageViewOnTouchListener;
import com.edwardinubuntu.dailykind.object.Idea;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
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
        suggestImageView.setOnTouchListener(new ImageViewOnTouchListener());

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

        getActivity().findViewById(R.id.home_idea_card_layout).setVisibility(View.GONE);

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

                                getActivity().findViewById(R.id.home_idea_card_layout).setVisibility(View.VISIBLE);

                                suggestImageView.setImageBitmap(null);

                                final Idea idea = new ParseObjectManager(parseObject).getIdea();

                                idea.setCategory(new ParseObjectManager(parseObject.getParseObject("categoryPointer")).getCategory());
                                randomIdeaTextView.setText(idea.getName());

                                TextView categoryTextView = (TextView)getActivity().findViewById(R.id.home_random_idea_category_text_view);
                                categoryTextView.setText(idea.getCategory().getName());

                                TextView captionTextView = (TextView)getActivity().findViewById(R.id.home_random_idea_caption_text_view);
                                captionTextView.setText(getActivity().getResources().getString(R.string.idea_caption_special_idea));

                                TextView descriptionTextView = (TextView)getActivity().findViewById(R.id.home_random_idea_description_text_view);
                                if (idea.getIdeaDescription() != null && idea.getIdeaDescription().length() > 0) {
                                    descriptionTextView.setText(idea.getIdeaDescription());
                                } else {
                                    descriptionTextView.setVisibility(View.GONE);
                                }

                                idea.setGraphic(new ParseObjectManager(parseObject.getParseObject("graphicPointer")).getGraphic());

                                suggestImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent(getActivity(), DeedContentActivity.class);
                                        intent.putExtra("ideaObjectId", idea.getObjectId());
                                        startActivity(intent);
                                    }
                                });

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
                                playLockSound();

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


    private void playLockSound() {
        //PLAY SOUND HERE
        AudioManager am = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        if  (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            Log.d(DailyKind.TAG, "Normal mode");
            MediaPlayer tabClick = MediaPlayer.create(getActivity(), R.raw.celebratory_cute_bells_double);
            tabClick.setLooping(false);
            tabClick.start();
        }
    }
}
