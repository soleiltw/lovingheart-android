package com.edwardinubuntu.dailykind.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoriesActivity;
import com.edwardinubuntu.dailykind.activity.DeedContentActivity;
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

    private View categoriesDeedLayout;

    private TextView randomIdeaTextView;

    private ProgressBar randomLoadingProgressBar;

    private ImageView suggestImageView;

    private LinearLayout.LayoutParams suggestImageViewLayoutParams;

    private Menu menu;

    private boolean queryLoading;

    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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

        categoriesDeedLayout = (View)rootView.findViewById(R.id.home_good_deed_categories);
        categoriesDeedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DeedCategoriesActivity.class);
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home, menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                queryRandomIdea();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void updateRefreshItem() {
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (isQueryLoading()) {
                    refreshItem.setActionView(R.layout.indeterminate_progress_action);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    private void queryRandomIdea() {

        setQueryLoading(true);
        updateRefreshItem();
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

                                View cardLayout = getActivity().findViewById(R.id.home_idea_card_layout);
                                cardLayout.setVisibility(View.VISIBLE);

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

                                cardLayout.setOnClickListener(new View.OnClickListener() {
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
                            setQueryLoading(false);
                            updateRefreshItem();
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

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }
}
