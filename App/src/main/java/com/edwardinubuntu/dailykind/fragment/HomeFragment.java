package com.edwardinubuntu.dailykind.fragment;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ProgressBar;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.DeedCategoriesActivity;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class HomeFragment extends PlaceholderFragment {


    private ProgressBar randomLoadingProgressBar;

    private Menu menu;

    private boolean queryLoading;

    private SharedPreferences preferences;

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

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        queryRandomIdea();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);



        randomLoadingProgressBar = (ProgressBar)rootView.findViewById(R.id.home_good_deed_random_progressBar);

        View categoriesDeedLayout = rootView.findViewById(R.id.browse_idea_button);
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

        final ParseQuery<ParseObject> randomIdeaQuery = new ParseQuery<ParseObject>("Idea");
        ArrayList<String> languageCollection = new ArrayList<String>();
        boolean englishDefaultValue = Locale.getDefault().getLanguage().contains("en");
        boolean supportEnglish = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_ENGLISH, englishDefaultValue);
        if (supportEnglish) {
            languageCollection.add("en");
        }
        boolean chineseDefaultValue = Locale.getDefault().getLanguage().contains("zh");
        boolean supportChinese = preferences.getBoolean(DailyKind.PREFERENCE_SUPPORT_CHINESE, chineseDefaultValue);
        if (supportChinese) {
            languageCollection.add("zh");
        }
        randomIdeaQuery.whereContainedIn("language", languageCollection);
        randomIdeaQuery.include("categoryPointer");
        randomIdeaQuery.include("graphicPointer");
        randomIdeaQuery.orderByDescending("createdAt");
        randomIdeaQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if  (parseObjects!=null && parseObjects.size() > 0) {

                    int maxIndex = parseObjects.size();
                    int randomIndex = (int)(Math.random() * maxIndex);

                    ParseObject randomParseObject = parseObjects.get(randomIndex);

                    HomeIdeaFragment homeIdeaFragment = new HomeIdeaFragment(randomParseObject);
                    homeIdeaFragment.setTitle(getActivity().getResources().getString(R.string.idea_caption_special_idea));
                    homeIdeaFragment.setTitleImageResource(R.drawable.ic_action_balloon);
                    getFragmentManager().beginTransaction().replace(R.id.home_random_idea_card_layout, homeIdeaFragment).commit();

                    HomeIdeaFragment latestIdeaFragment = new HomeIdeaFragment(parseObjects.get(0));
                    latestIdeaFragment.setTitle(getActivity().getResources().getString(R.string.idea_caption_latest_idea));
                    latestIdeaFragment.setTitleImageResource(R.drawable.ic_action_emo_basic);
                    getFragmentManager().beginTransaction().replace(R.id.home_latest_idea_card_layout, latestIdeaFragment).commit();

                }

                playBellsSound();

                randomLoadingProgressBar.setVisibility(View.GONE);
                setQueryLoading(false);
                updateRefreshItem();
            }
        });
    }


    private void playBellsSound() {
        //PLAY SOUND HERE
        AudioManager am = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
        if  (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            MediaPlayer tabClick = MediaPlayer.create(getActivity(), R.raw.celebratory_cute_bells_double);
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
