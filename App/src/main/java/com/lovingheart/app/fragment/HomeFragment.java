package com.lovingheart.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.ProgressBar;
import com.google.analytics.tracking.android.Fields;
import com.lovingheart.app.DailyKind;
import com.lovingheart.app.R;
import com.lovingheart.app.activity.DeedCategoriesActivity;
import com.lovingheart.app.adapter.IdeaCardArrayAdapter;
import com.lovingheart.app.object.IdeaObject;
import com.lovingheart.app.util.AnalyticsManager;
import com.lovingheart.app.view.ExpandableListView;
import com.parse.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class HomeFragment extends PlaceholderFragment {


    private ProgressBar randomLoadingProgressBar;

    private Menu menu;

    private SharedPreferences preferences;

    private IdeaCardArrayAdapter ideaCardArrayAdapter;

    private List<IdeaObject> ideaObjectList;

    private ParseQuery<ParseObject> todayIdeaQuery;

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

        ideaObjectList = new ArrayList<IdeaObject>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity()!=null) {
            ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.title_today));
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        todayIdeaQuery = new ParseQuery<ParseObject>("Today");

        ideaObjectList.clear();
        ideaCardArrayAdapter.notifyDataSetChanged();
        queryAllIdea();
        queryTodayIdea();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        ExpandableListView cardListView = (ExpandableListView)rootView.findViewById(R.id.home_group_idea_card_layout);
        cardListView.setExpand(true);

        ideaCardArrayAdapter = new IdeaCardArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, ideaObjectList);
        cardListView.setAdapter(ideaCardArrayAdapter);

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
                ideaObjectList.clear();
                ideaCardArrayAdapter.notifyDataSetChanged();
                queryAllIdea();

                todayIdeaQuery.clearCachedResult();
                queryTodayIdea();
                break;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateRefreshItem(boolean isLoading) {
        if (menu != null) {
            MenuItem refreshItem = menu.findItem(R.id.action_reload);
            if (refreshItem != null) {
                if (isLoading) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.indeterminate_progress_action);
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }

    private void queryTodayIdea() {

        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("Feature Idea");
        todayIdeaQuery.whereEqualTo("type", "Feature Idea");

        ArrayList<String> statusCollection = new ArrayList<String>();
        statusCollection.add("close");
        todayIdeaQuery.whereNotContainedIn("status", statusCollection);

        todayIdeaQuery.include("ideaPointer");
        todayIdeaQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        todayIdeaQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
        todayIdeaQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null && parseObjects.size() > 0) {
                    for (ParseObject eachParseObject : parseObjects) {

                        if (eachParseObject.has("ideaPointer")) {

                            ParseQuery<ParseObject> ideaObjectQuery = new ParseQuery<ParseObject>("Idea");
                            ideaObjectQuery.whereEqualTo("objectId", eachParseObject.getParseObject("ideaPointer").getObjectId());
                            ideaObjectQuery.include("categoryPointer");
                            ideaObjectQuery.include("graphicPointer");

                            ideaObjectQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
                            ideaObjectQuery.setMaxCacheAge(DailyKind.QUERY_AT_LEAST_CACHE_AGE);
                            ideaObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(final ParseObject parseObject, ParseException e) {
                                    if (parseObject!= null) {
                                        IdeaObject ideaLatestObject = new IdeaObject(parseObject);
                                        ideaLatestObject.setTitleResource(R.string.idea_caption_feature_idea);
                                        ideaLatestObject.setTitleImageResource(R.drawable.ic_action_emo_basic);


                                        Log.d(DailyKind.TAG, "IdeaObject parseObject: " + parseObject.getObjectId());

                                        // Add to first one
                                        ideaObjectList.add(0, ideaLatestObject);
                                        ideaCardArrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            });


                        }
                    }

                }
            }
        });
    }

    private void queryAllIdea() {

        updateRefreshItem(true);
        randomLoadingProgressBar.setVisibility(View.VISIBLE);

        final ParseQuery<ParseObject> randomIdeaQuery = new ParseQuery<ParseObject>("Idea");

        randomIdeaQuery.whereContainedIn("language", DailyKind.getLanguageCollection(getActivity()));
        randomIdeaQuery.include("categoryPointer");
        randomIdeaQuery.include("graphicPointer");
        ArrayList<String> stringCollection = new ArrayList<String>();
        stringCollection.add("close");
        randomIdeaQuery.whereNotContainedIn("status", stringCollection);
        randomIdeaQuery.orderByDescending("createdAt");
        randomIdeaQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        randomIdeaQuery.setMaxCacheAge(DailyKind.QUERY_MAX_CACHE_AGE);
        randomIdeaQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if  (parseObjects!=null && parseObjects.size() > 0) {

                    int maxIndex = parseObjects.size();
                    int randomIndex = (int)(Math.random() * maxIndex);

                    ParseObject randomParseObject = parseObjects.get(randomIndex);

                    IdeaObject ideaRandomObject = new IdeaObject(randomParseObject);
                    ideaRandomObject.setTitleResource(R.string.idea_caption_special_idea);
                    ideaRandomObject.setTitleImageResource(R.drawable.ic_action_balloon);
                    ideaObjectList.add(ideaRandomObject);

                    IdeaObject ideaLatestObject = new IdeaObject(parseObjects.get(0));
                    ideaLatestObject.setTitleResource(R.string.idea_caption_latest_idea);
                    ideaLatestObject.setTitleImageResource(R.drawable.ic_action_emo_basic);
                    ideaObjectList.add(ideaLatestObject);

                    ideaCardArrayAdapter.notifyDataSetChanged();
                }

                playBellsSound();

                randomLoadingProgressBar.setVisibility(View.GONE);
                updateRefreshItem(false);
            }
        });
    }


    private void playBellsSound() {
        //PLAY SOUND HERE
        if (getActivity()!=null) {
            AudioManager am = (AudioManager)getActivity().getSystemService(Context.AUDIO_SERVICE);
            boolean isPlayingSound = preferences.getBoolean(DailyKind.PREFERENCE_PLAYING_SOUND, true);
            if  (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && isPlayingSound) {
            MediaPlayer tabClick = MediaPlayer.create(getActivity(), R.raw.celebratory_cute_bells_double);
            tabClick.start();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        HashMap<String, String> gaParams = new HashMap<String, String>();
        gaParams.put(Fields.SCREEN_NAME, "Home");
        gaParams.put(Fields.EVENT_ACTION, "View");
        gaParams.put(Fields.EVENT_CATEGORY, "Home");
        gaParams.put(Fields.EVENT_LABEL, "All");
        AnalyticsManager.getInstance().getGaTracker().send(gaParams);
    }
}
