package com.edwardinubuntu.dailykind.fragment;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import com.edwardinubuntu.dailykind.DailyKind;
import com.edwardinubuntu.dailykind.R;
import com.edwardinubuntu.dailykind.activity.LoginActivity;
import com.edwardinubuntu.dailykind.adapter.GalleryArrayAdapter;
import com.edwardinubuntu.dailykind.object.Graphic;
import com.edwardinubuntu.dailykind.object.UserImpact;
import com.edwardinubuntu.dailykind.util.CircleTransform;
import com.edwardinubuntu.dailykind.util.parse.ParseObjectManager;
import com.edwardinubuntu.dailykind.view.ExpandableGridView;
import com.parse.*;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward_chiang on 2013/11/23.
 */
public class MeFragment extends PlaceholderFragment {

    private TextView storiesSharedCountTextView;

    private TextView graphicEarnedCountTextView;

    private TextView userNameTextView;

    private TextView sinceTextView;

    private TextView reviewStarsTextView;

    private ExpandableGridView galleryGridView;

    private GalleryArrayAdapter galleryArrayAdapter;

    private List<Graphic> userGraphicsList;

    private UserImpact userImpactInfo;

    private Menu menu;

    private boolean queryLoading;

    public static MeFragment newInstance(int sectionNumber) {
        MeFragment fragment = new MeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        userGraphicsList = new ArrayList<Graphic>();
        galleryArrayAdapter = new GalleryArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, userGraphicsList);

        userImpactInfo = new UserImpact();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_me, container, false);

        userNameTextView = (TextView)rootView.findViewById(R.id.user_name_text_view);

        sinceTextView = (TextView)rootView.findViewById(R.id.me_since_text_view);

        storiesSharedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_stories_share_text_view);
        graphicEarnedCountTextView = (TextView)rootView.findViewById(R.id.user_impact_graphic_earned_text_view);

        galleryGridView = (ExpandableGridView)rootView.findViewById(R.id.me_graphic_gallery_grid_view);
        galleryGridView.setExpand(true);
        galleryGridView.setNumColumns(3);
        galleryGridView.setAdapter(galleryArrayAdapter);

        reviewStarsTextView = (TextView)rootView.findViewById(R.id.user_impact_review_stars_text_view);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProfile();
    }

    private void loadProfile() {
        if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {

            userNameTextView.setText(ParseUser.getCurrentUser().getString("name"));

            if (ParseUser.getCurrentUser().getString("name") == null) {
                ParseUser.getCurrentUser().fetchInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        userNameTextView.setText(parseObject.getString("name"));
                    }
                });
            }

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            if  (sinceTextView!=null && ParseUser.getCurrentUser().getCreatedAt()!=null) {
                sinceTextView.setText(getString(R.string.me_since_pre_text) + " " + dateFormat.format(ParseUser.getCurrentUser().getCreatedAt()));
            }


            getActivity().findViewById(R.id.me_profile_layout).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.me_ask_login_layout).setVisibility(View.GONE);

            if (ParseUser.getCurrentUser().has("avatar")) {
                ParseObject avatarObject = ParseUser.getCurrentUser().getParseObject("avatar");

                avatarObject.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        ImageView avatarImageView = (ImageView)getActivity().findViewById(R.id.user_avatar_image_view);
                        if (parseObject!=null && parseObject.getString("imageType").equals("url")
                                && getActivity() != null && avatarImageView != null) {
                            Picasso.with(getActivity())
                                    .load(parseObject.getString("imageUrl"))
                                    .transform(new CircleTransform())
                                    .into(avatarImageView);
                        }
                    }
                });
            }

            // Load user impact
            loadUserImpact();

        } else {
            getActivity().findViewById(R.id.me_profile_layout).setVisibility(View.GONE);
            getActivity().findViewById(R.id.me_ask_login_layout).setVisibility(View.VISIBLE);

            getActivity().findViewById(R.id.me_ask_login_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(loginIntent);
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.me, menu);
        this.menu = menu;
    }

    private void queryStories() {
        ParseQuery<ParseObject> storyQuery = new ParseQuery<ParseObject>("Story");
        storyQuery.whereEqualTo("StoryTeller", ParseUser.getCurrentUser());
        storyQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (parseObjects != null) {

                    userImpactInfo.setStoriesSharedCount(parseObjects.size());
                    storiesSharedCountTextView.setText(String.valueOf(parseObjects.size()));

                    int reviewImpactCount = 0;
                    for (ParseObject eachStory : parseObjects) {
                        if (eachStory.has("reviewImpact")) {
                            reviewImpactCount += eachStory.getInt("reviewImpact");
                        }
                    }
                    reviewStarsTextView.setText(String.valueOf(reviewImpactCount));
                    userImpactInfo.setStarsReviewCount(reviewImpactCount);

                    saveUserImpact(userImpactInfo);
                }
            }
        });
    }


    private void queryGraphicEarned() {
        ParseQuery<ParseObject> graphicsEarnedQuery = ParseQuery.getQuery("GraphicsEarned");
        graphicsEarnedQuery.whereEqualTo("userId", ParseUser.getCurrentUser());
        graphicsEarnedQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK);
        graphicsEarnedQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    ParseRelation graphicsRelation = parseObject.getRelation("graphicsEarned");
                    ParseQuery<ParseObject> graphicsEarnedQuery = graphicsRelation.getQuery();
                    graphicsEarnedQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if (parseObjects!=null && !parseObjects.isEmpty()) {

                                graphicEarnedCountTextView.setText(String.valueOf(parseObjects.size()));
                                userImpactInfo.setGraphicEarnedCount(parseObjects.size());


                                if (getActivity()!=null && !parseObjects.isEmpty() && getActivity().findViewById(R.id.me_graphic_gallery_layout) != null) {
                                    getActivity().findViewById(R.id.me_graphic_gallery_layout).setVisibility(View.VISIBLE);
                                }

                                userGraphicsList.clear();
                                for (ParseObject eachGraphicObject : parseObjects) {
                                    Graphic graphic = new ParseObjectManager(eachGraphicObject).getGraphic();
                                    userGraphicsList.add(graphic);
                                }
                                galleryArrayAdapter.notifyDataSetChanged();

                                updateUserImpact(userImpactInfo);
                            } else {
                                graphicEarnedCountTextView.setText(String.valueOf(0));
                                userGraphicsList.clear();
                                galleryArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    private void loadUserImpact() {
        setQueryLoading(true);
        updateRefreshItem();

        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (parseObject!=null) {
                    if (parseObject.has("sharedStoriesCount")) {
                        storiesSharedCountTextView.setText(String.valueOf(parseObject.getInt("sharedStoriesCount")));
                    }
                    if (parseObject.has("graphicsEarnedCount")) {
                        int graphicsEarnedCount = parseObject.getInt("graphicsEarnedCount");
                        graphicEarnedCountTextView.setText(String.valueOf(graphicsEarnedCount));
                        if (graphicsEarnedCount >0) {
                            queryGraphicEarned();
                        }
                    }
                    if (parseObject.has("reviewStarsImpact")) {
                        reviewStarsTextView.setText(String.valueOf(parseObject.getInt("reviewStarsImpact")));
                    }
                }
                setQueryLoading(false);
                updateRefreshItem();

            }
        });
    }

    private void updateUserImpact(final UserImpact userImpactInfo) {
        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }
                if (parseObject!=null) {
                    ParseObject userImpactObject = parseObject;

                    if (userImpactInfo.getStoriesSharedCount() > 0){
                        userImpactObject.put("sharedStoriesCount", userImpactInfo.getStoriesSharedCount());
                    }
                    if (userImpactInfo.getGraphicEarnedCount() > 0){
                        userImpactObject.put("graphicsEarnedCount", userImpactInfo.getGraphicEarnedCount());
                    }
                    if (userImpactInfo.getStarsReviewCount() > 0){
                        userImpactObject.put("reviewStarsImpact", userImpactInfo.getStarsReviewCount());
                    }
                    userImpactObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if  (e==null && getActivity()!=null) {
                                Log.d(DailyKind.TAG, "User Impact report updated.");
                            }
                        }
                    });
                }



            }
        });
    }

    private void saveUserImpact(final UserImpact userImpactInfo) {
        ParseQuery<ParseObject> userImpactQuery = new ParseQuery<ParseObject>("UserImpact");
        userImpactQuery.whereEqualTo("User", ParseUser.getCurrentUser());
        userImpactQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {

                if (e != null) {
                    Log.e(DailyKind.TAG, e.getLocalizedMessage());
                }

                ParseObject userImpactObject = new ParseObject("UserImpact");
                userImpactObject.put("User", ParseUser.getCurrentUser());
                if (parseObject!=null) {
                    userImpactObject = parseObject;
                }

                if (userImpactInfo.getStoriesSharedCount() > 0){
                    userImpactObject.put("sharedStoriesCount", userImpactInfo.getStoriesSharedCount());
                }
                if (userImpactInfo.getGraphicEarnedCount() > 0){
                    userImpactObject.put("graphicsEarnedCount", userImpactInfo.getGraphicEarnedCount());
                }
                if (userImpactInfo.getStarsReviewCount() > 0){
                    userImpactObject.put("reviewStarsImpact", userImpactInfo.getStarsReviewCount());
                }
                userImpactObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if  (e==null && getActivity()!=null) {
                            Log.d(DailyKind.TAG, "User Impact report updated.");
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_reload: {
                loadProfile();
                queryStories();
                queryGraphicEarned();
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

    public boolean isQueryLoading() {
        return queryLoading;
    }

    public void setQueryLoading(boolean queryLoading) {
        this.queryLoading = queryLoading;
    }
}
